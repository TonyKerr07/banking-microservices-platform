package br.com.antonio.banking.pix.service;

import br.com.antonio.banking.common.exception.ConflictException;
import br.com.antonio.banking.common.exception.UnprocessableException;
import br.com.antonio.banking.pix.domain.entity.PixKey;
import br.com.antonio.banking.pix.domain.entity.PixTransaction;
import br.com.antonio.banking.pix.domain.enums.PixKeyStatus;
import br.com.antonio.banking.pix.domain.enums.PixKeyType;
import br.com.antonio.banking.pix.domain.enums.PixTransactionStatus;
import br.com.antonio.banking.pix.dto.request.CreatePixKeyRequest;
import br.com.antonio.banking.pix.dto.request.SendPixRequest;
import br.com.antonio.banking.pix.dto.response.PixKeyResponse;
import br.com.antonio.banking.pix.dto.response.PixTransactionResponse;
import br.com.antonio.banking.pix.exception.PixKeyNotFoundException;
import br.com.antonio.banking.pix.mapper.PixMapper;
import br.com.antonio.banking.pix.repository.PixKeyRepository;
import br.com.antonio.banking.pix.repository.PixTransactionRepository;
import br.com.antonio.banking.pix.service.impl.PixServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PixServiceImpl")
class PixServiceImplTest {

    @Mock
    private PixKeyRepository pixKeyRepository;

    @Mock
    private PixTransactionRepository pixTransactionRepository;

    @Mock
    private PixMapper pixMapper;

    @InjectMocks
    private PixServiceImpl pixService;

    private UUID accountId;
    private PixKey activePixKey;
    private PixKeyResponse pixKeyResponse;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();

        activePixKey = PixKey.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .keyType(PixKeyType.EMAIL)
                .keyValue("joao@email.com")
                .status(PixKeyStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        pixKeyResponse = new PixKeyResponse(
                activePixKey.getId(), accountId,
                PixKeyType.EMAIL, "joao@email.com",
                PixKeyStatus.ACTIVE, Instant.now()
        );
    }

    @Nested
    @DisplayName("registerKey()")
    class RegisterKey {

        @Test
        @DisplayName("should register a new PIX key successfully")
        void shouldRegisterKeySuccessfully() {
            var request = new CreatePixKeyRequest(accountId, PixKeyType.EMAIL, "joao@email.com");

            when(pixKeyRepository.countByAccountIdAndStatus(accountId, PixKeyStatus.ACTIVE))
                    .thenReturn(0L);
            when(pixKeyRepository.existsByKeyValueAndStatus("joao@email.com", PixKeyStatus.ACTIVE))
                    .thenReturn(false);
            when(pixKeyRepository.save(any(PixKey.class))).thenReturn(activePixKey);
            when(pixMapper.toKeyResponse(activePixKey)).thenReturn(pixKeyResponse);

            PixKeyResponse result = pixService.registerKey(request);

            assertThat(result).isNotNull();
            assertThat(result.keyValue()).isEqualTo("joao@email.com");
            assertThat(result.keyType()).isEqualTo(PixKeyType.EMAIL);
        }

        @Test
        @DisplayName("should throw ConflictException when key already exists")
        void shouldThrowConflictWhenKeyExists() {
            var request = new CreatePixKeyRequest(accountId, PixKeyType.EMAIL, "joao@email.com");

            when(pixKeyRepository.countByAccountIdAndStatus(accountId, PixKeyStatus.ACTIVE))
                    .thenReturn(1L);
            when(pixKeyRepository.existsByKeyValueAndStatus("joao@email.com", PixKeyStatus.ACTIVE))
                    .thenReturn(true);

            assertThatThrownBy(() -> pixService.registerKey(request))
                    .isInstanceOf(ConflictException.class);

            verify(pixKeyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UnprocessableException when account has 5 keys")
        void shouldThrowWhenMaxKeysReached() {
            var request = new CreatePixKeyRequest(accountId, PixKeyType.EMAIL, "joao@email.com");

            when(pixKeyRepository.countByAccountIdAndStatus(accountId, PixKeyStatus.ACTIVE))
                    .thenReturn(5L);

            assertThatThrownBy(() -> pixService.registerKey(request))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("maximum");

            verify(pixKeyRepository, never()).save(any());
        }

        @Test
        @DisplayName("should auto-generate value for RANDOM key type")
        void shouldAutoGenerateRandomKey() {
            var request = new CreatePixKeyRequest(accountId, PixKeyType.RANDOM, null);

            when(pixKeyRepository.countByAccountIdAndStatus(accountId, PixKeyStatus.ACTIVE))
                    .thenReturn(0L);
            when(pixKeyRepository.existsByKeyValueAndStatus(any(), eq(PixKeyStatus.ACTIVE)))
                    .thenReturn(false);
            when(pixKeyRepository.save(any(PixKey.class))).thenReturn(activePixKey);
            when(pixMapper.toKeyResponse(any())).thenReturn(pixKeyResponse);

            pixService.registerKey(request);

            verify(pixKeyRepository).save(argThat(key ->
                    key.getKeyType() == PixKeyType.RANDOM && key.getKeyValue() != null
            ));
        }
    }

    @Nested
    @DisplayName("deleteKey()")
    class DeleteKey {

        @Test
        @DisplayName("should soft-delete an active PIX key")
        void shouldSoftDeleteKey() {
            UUID id = activePixKey.getId();
            when(pixKeyRepository.findById(id)).thenReturn(Optional.of(activePixKey));
            when(pixKeyRepository.save(activePixKey)).thenReturn(activePixKey);

            pixService.deleteKey(id);

            assertThat(activePixKey.getStatus()).isEqualTo(PixKeyStatus.DELETED);
            assertThat(activePixKey.getDeletedAt()).isNotNull();
            verify(pixKeyRepository).save(activePixKey);
        }

        @Test
        @DisplayName("should throw UnprocessableException when key is already deleted")
        void shouldThrowWhenAlreadyDeleted() {
            activePixKey.delete();
            UUID id = activePixKey.getId();
            when(pixKeyRepository.findById(id)).thenReturn(Optional.of(activePixKey));

            assertThatThrownBy(() -> pixService.deleteKey(id))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("already deleted");
        }

        @Test
        @DisplayName("should throw PixKeyNotFoundException when key not found")
        void shouldThrowWhenKeyNotFound() {
            UUID id = UUID.randomUUID();
            when(pixKeyRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pixService.deleteKey(id))
                    .isInstanceOf(PixKeyNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("sendPix()")
    class SendPix {

        @Test
        @DisplayName("should send PIX successfully")
        void shouldSendPixSuccessfully() {
            UUID senderAccountId = UUID.randomUUID();
            var request = new SendPixRequest(
                    senderAccountId, "joao@email.com", BigDecimal.valueOf(100), "Pagamento"
            );

            PixTransaction tx = PixTransaction.builder()
                    .id(UUID.randomUUID())
                    .sourceAccountId(senderAccountId)
                    .targetPixKey("joao@email.com")
                    .targetAccountId(accountId)
                    .amount(BigDecimal.valueOf(100))
                    .status(PixTransactionStatus.COMPLETED)
                    .endToEndId("E" + UUID.randomUUID().toString().replace("-", "").substring(0, 32))
                    .createdAt(Instant.now())
                    .build();

            PixTransactionResponse txResponse = new PixTransactionResponse(
                    tx.getId(), senderAccountId, "joao@email.com", accountId,
                    BigDecimal.valueOf(100), "Pagamento",
                    PixTransactionStatus.COMPLETED, tx.getEndToEndId(), Instant.now()
            );

            when(pixKeyRepository.existsByKeyValueAndStatus("joao@email.com", PixKeyStatus.ACTIVE))
                    .thenReturn(true);
            when(pixKeyRepository.findByKeyValueAndStatus("joao@email.com", PixKeyStatus.ACTIVE))
                    .thenReturn(Optional.of(activePixKey));
            when(pixTransactionRepository.save(any(PixTransaction.class))).thenReturn(tx);
            when(pixMapper.toTransactionResponse(tx)).thenReturn(txResponse);

            PixTransactionResponse result = pixService.sendPix(request);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(PixTransactionStatus.COMPLETED);
            assertThat(result.endToEndId()).isNotNull();
        }

        @Test
        @DisplayName("should throw UnprocessableException when sending PIX to yourself")
        void shouldThrowWhenSendingToYourself() {
            var request = new SendPixRequest(
                    accountId, "joao@email.com", BigDecimal.valueOf(100), "Teste"
            );

            when(pixKeyRepository.existsByKeyValueAndStatus("joao@email.com", PixKeyStatus.ACTIVE))
                    .thenReturn(true);
            when(pixKeyRepository.findByKeyValueAndStatus("joao@email.com", PixKeyStatus.ACTIVE))
                    .thenReturn(Optional.of(activePixKey));

            assertThatThrownBy(() -> pixService.sendPix(request))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("yourself");
        }
    }
}
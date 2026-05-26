package br.com.antonio.banking.transfers.service;

import br.com.antonio.banking.common.exception.ResourceNotFoundException;
import br.com.antonio.banking.common.exception.UnprocessableException;
import br.com.antonio.banking.transfers.client.AccountsClient;
import br.com.antonio.banking.transfers.client.dto.AccountInfo;
import br.com.antonio.banking.transfers.domain.entity.Transfer;
import br.com.antonio.banking.transfers.domain.enums.TransferStatus;
import br.com.antonio.banking.transfers.domain.enums.TransferType;
import br.com.antonio.banking.transfers.dto.request.CreateTransferRequest;
import br.com.antonio.banking.transfers.dto.response.TransferResponse;
import br.com.antonio.banking.transfers.exception.TransferNotFoundException;
import br.com.antonio.banking.transfers.mapper.TransferMapper;
import br.com.antonio.banking.transfers.repository.TransferRepository;
import br.com.antonio.banking.transfers.service.impl.TransferServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferServiceImpl")
class TransferServiceImplTest {

    @Mock private TransferRepository transferRepository;
    @Mock private TransferMapper transferMapper;
    @Mock private AccountsClient accountsClient;

    @InjectMocks
    private TransferServiceImpl transferService;

    private UUID sourceId;
    private UUID targetId;
    private Transfer transfer;
    private TransferResponse transferResponse;
    private AccountInfo activeSource;
    private AccountInfo activeTarget;

    @BeforeEach
    void setUp() {
        sourceId = UUID.randomUUID();
        targetId = UUID.randomUUID();

        activeSource = new AccountInfo(sourceId, "ACTIVE", BigDecimal.valueOf(2000));
        activeTarget = new AccountInfo(targetId, "ACTIVE", BigDecimal.ZERO);

        transfer = Transfer.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(sourceId)
                .targetAccountId(targetId)
                .amount(BigDecimal.valueOf(500))
                .description("Teste")
                .transferType(TransferType.INTERNAL)
                .status(TransferStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();

        transferResponse = new TransferResponse(
                transfer.getId(), sourceId, targetId,
                BigDecimal.valueOf(500), "Teste",
                TransferType.INTERNAL, TransferStatus.COMPLETED,
                null, Instant.now()
        );
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create transfer successfully when both accounts are active")
        void shouldCreateTransferSuccessfully() {
            var request = new CreateTransferRequest(
                    sourceId, targetId, BigDecimal.valueOf(500), "Teste", TransferType.INTERNAL
            );

            when(accountsClient.findById(sourceId)).thenReturn(Optional.of(activeSource));
            when(accountsClient.findById(targetId)).thenReturn(Optional.of(activeTarget));
            when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
            when(transferMapper.toResponse(transfer)).thenReturn(transferResponse);

            TransferResponse result = transferService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.sourceAccountId()).isEqualTo(sourceId);
            assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(500));
            verify(accountsClient).findById(sourceId);
            verify(accountsClient).findById(targetId);
            verify(transferRepository).save(any(Transfer.class));
        }

        @Test
        @DisplayName("should throw UnprocessableException when source equals target")
        void shouldThrowWhenSameAccount() {
            var request = new CreateTransferRequest(
                    sourceId, sourceId, BigDecimal.valueOf(100), "Teste", TransferType.INTERNAL
            );

            assertThatThrownBy(() -> transferService.create(request))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("different");

            verify(accountsClient, never()).findById(any());
            verify(transferRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when source account does not exist")
        void shouldThrowWhenSourceAccountNotFound() {
            var request = new CreateTransferRequest(
                    sourceId, targetId, BigDecimal.valueOf(100), "Teste", TransferType.INTERNAL
            );

            when(accountsClient.findById(sourceId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transferRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UnprocessableException when source account is blocked")
        void shouldThrowWhenSourceAccountNotActive() {
            var request = new CreateTransferRequest(
                    sourceId, targetId, BigDecimal.valueOf(100), "Teste", TransferType.INTERNAL
            );
            var blockedSource = new AccountInfo(sourceId, "BLOCKED", BigDecimal.valueOf(1000));

            when(accountsClient.findById(sourceId)).thenReturn(Optional.of(blockedSource));

            assertThatThrownBy(() -> transferService.create(request))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("Source account is not active");

            verify(transferRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UnprocessableException when target account is closed")
        void shouldThrowWhenTargetAccountNotActive() {
            var request = new CreateTransferRequest(
                    sourceId, targetId, BigDecimal.valueOf(100), "Teste", TransferType.INTERNAL
            );
            var closedTarget = new AccountInfo(targetId, "CLOSED", BigDecimal.ZERO);

            when(accountsClient.findById(sourceId)).thenReturn(Optional.of(activeSource));
            when(accountsClient.findById(targetId)).thenReturn(Optional.of(closedTarget));

            assertThatThrownBy(() -> transferService.create(request))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("Target account is not active");

            verify(transferRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return transfer when found")
        void shouldReturnTransferWhenFound() {
            UUID id = transfer.getId();
            when(transferRepository.findById(id)).thenReturn(Optional.of(transfer));
            when(transferMapper.toResponse(transfer)).thenReturn(transferResponse);

            TransferResponse result = transferService.findById(id);

            assertThat(result.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw TransferNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(transferRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.findById(id))
                    .isInstanceOf(TransferNotFoundException.class);
        }
    }
}
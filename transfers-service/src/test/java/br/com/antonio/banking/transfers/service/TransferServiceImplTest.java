package br.com.antonio.banking.transfers.service;

import br.com.antonio.banking.common.exception.UnprocessableException;
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

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper transferMapper;

    @InjectMocks
    private TransferServiceImpl transferService;

    private UUID sourceId;
    private UUID targetId;
    private Transfer transfer;
    private TransferResponse transferResponse;

    @BeforeEach
    void setUp() {
        sourceId = UUID.randomUUID();
        targetId = UUID.randomUUID();

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
        @DisplayName("should create transfer successfully")
        void shouldCreateTransferSuccessfully() {
            var request = new CreateTransferRequest(
                    sourceId, targetId, BigDecimal.valueOf(500), "Teste", TransferType.INTERNAL
            );

            when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
            when(transferMapper.toResponse(transfer)).thenReturn(transferResponse);

            TransferResponse result = transferService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.sourceAccountId()).isEqualTo(sourceId);
            assertThat(result.targetAccountId()).isEqualTo(targetId);
            assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(500));
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
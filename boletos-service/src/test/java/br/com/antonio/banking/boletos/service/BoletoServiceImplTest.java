package br.com.antonio.banking.boletos.service;

import br.com.antonio.banking.boletos.domain.entity.Boleto;
import br.com.antonio.banking.boletos.domain.enums.BoletoStatus;
import br.com.antonio.banking.boletos.dto.request.CreateBoletoRequest;
import br.com.antonio.banking.boletos.dto.response.BoletoResponse;
import br.com.antonio.banking.boletos.exception.BoletoNotFoundException;
import br.com.antonio.banking.boletos.mapper.BoletoMapper;
import br.com.antonio.banking.boletos.repository.BoletoRepository;
import br.com.antonio.banking.boletos.service.impl.BoletoServiceImpl;
import br.com.antonio.banking.common.exception.UnprocessableException;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoletoServiceImpl")
class BoletoServiceImplTest {

    @Mock
    private BoletoRepository boletoRepository;

    @Mock
    private BoletoMapper boletoMapper;

    @InjectMocks
    private BoletoServiceImpl boletoService;

    private UUID payerAccountId;
    private Boleto pendingBoleto;
    private BoletoResponse boletoResponse;

    @BeforeEach
    void setUp() {
        payerAccountId = UUID.randomUUID();

        pendingBoleto = Boleto.builder()
                .id(UUID.randomUUID())
                .barCode("00191234567890123456789012345678901234567890123456")
                .payerAccountId(payerAccountId)
                .beneficiaryName("Empresa XPTO")
                .amount(BigDecimal.valueOf(350))
                .dueDate(LocalDate.now().plusDays(30))
                .status(BoletoStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        boletoResponse = new BoletoResponse(
                pendingBoleto.getId(),
                pendingBoleto.getBarCode(),
                payerAccountId,
                "Empresa XPTO",
                BigDecimal.valueOf(350),
                LocalDate.now().plusDays(30),
                BoletoStatus.PENDING,
                null,
                Instant.now(),
                null
        );
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should issue boleto successfully")
        void shouldIssueBoletoSuccessfully() {
            var request = new CreateBoletoRequest(
                    payerAccountId, "Empresa XPTO", null,
                    BigDecimal.valueOf(350), LocalDate.now().plusDays(30), null
            );

            when(boletoRepository.save(any(Boleto.class))).thenReturn(pendingBoleto);
            when(boletoMapper.toResponse(pendingBoleto)).thenReturn(boletoResponse);

            BoletoResponse result = boletoService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(BoletoStatus.PENDING);
            assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(350));
            verify(boletoRepository).save(any(Boleto.class));
        }
    }

    @Nested
    @DisplayName("pay()")
    class Pay {

        @Test
        @DisplayName("should pay a pending boleto successfully")
        void shouldPayPendingBoleto() {
            UUID id = pendingBoleto.getId();
            when(boletoRepository.findById(id)).thenReturn(Optional.of(pendingBoleto));
            when(boletoRepository.save(pendingBoleto)).thenReturn(pendingBoleto);
            when(boletoMapper.toResponse(pendingBoleto)).thenReturn(boletoResponse);

            boletoService.pay(id);

            verify(boletoRepository).save(pendingBoleto);
            assertThat(pendingBoleto.getStatus()).isEqualTo(BoletoStatus.PAID);
            assertThat(pendingBoleto.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw UnprocessableException when boleto is already paid")
        void shouldThrowWhenAlreadyPaid() {
            pendingBoleto.setStatus(BoletoStatus.PAID);
            UUID id = pendingBoleto.getId();
            when(boletoRepository.findById(id)).thenReturn(Optional.of(pendingBoleto));

            assertThatThrownBy(() -> boletoService.pay(id))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("cannot be paid");
        }

        @Test
        @DisplayName("should throw UnprocessableException when boleto is cancelled")
        void shouldThrowWhenCancelled() {
            pendingBoleto.setStatus(BoletoStatus.CANCELLED);
            UUID id = pendingBoleto.getId();
            when(boletoRepository.findById(id)).thenReturn(Optional.of(pendingBoleto));

            assertThatThrownBy(() -> boletoService.pay(id))
                    .isInstanceOf(UnprocessableException.class);
        }
    }

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("should cancel a pending boleto successfully")
        void shouldCancelPendingBoleto() {
            UUID id = pendingBoleto.getId();
            when(boletoRepository.findById(id)).thenReturn(Optional.of(pendingBoleto));
            when(boletoRepository.save(pendingBoleto)).thenReturn(pendingBoleto);
            when(boletoMapper.toResponse(pendingBoleto)).thenReturn(boletoResponse);

            boletoService.cancel(id);

            assertThat(pendingBoleto.getStatus()).isEqualTo(BoletoStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw BoletoNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(boletoRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> boletoService.cancel(id))
                    .isInstanceOf(BoletoNotFoundException.class);
        }
    }
}
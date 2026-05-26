package br.com.antonio.banking.boletos.service.impl;

import br.com.antonio.banking.boletos.domain.entity.Boleto;
import br.com.antonio.banking.boletos.dto.request.CreateBoletoRequest;
import br.com.antonio.banking.boletos.dto.response.BoletoResponse;
import br.com.antonio.banking.boletos.exception.BoletoNotFoundException;
import br.com.antonio.banking.boletos.mapper.BoletoMapper;
import br.com.antonio.banking.boletos.repository.BoletoRepository;
import br.com.antonio.banking.boletos.service.BoletoService;
import br.com.antonio.banking.common.dto.PageResponse;
import br.com.antonio.banking.common.exception.UnprocessableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoletoServiceImpl implements BoletoService {

    private final BoletoRepository boletoRepository;
    private final BoletoMapper boletoMapper;

    @Override
    @Transactional
    public BoletoResponse create(CreateBoletoRequest request) {
        log.info("Issuing boleto for account {} — amount: {}",
                request.payerAccountId(), request.amount());

        Boleto boleto = Boleto.builder()
                .barCode(generateBarCode())
                .payerAccountId(request.payerAccountId())
                .beneficiaryName(request.beneficiaryName())
                .beneficiaryDocument(request.beneficiaryDocument())
                .amount(request.amount())
                .dueDate(request.dueDate())
                .description(request.description())
                .build();

        Boleto saved = boletoRepository.save(boleto);
        log.info("Boleto issued: id={}, barCode={}", saved.getId(), saved.getBarCode());
        return boletoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BoletoResponse findById(UUID id) {
        return boletoMapper.toResponse(getBoletoOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BoletoResponse> findAll(Pageable pageable) {
        return PageResponse.from(boletoRepository.findAll(pageable), boletoMapper::toResponse);
    }

    @Override
    @Transactional
    public BoletoResponse pay(UUID id) {
        Boleto boleto = getBoletoOrThrow(id);

        if (!boleto.isPending()) {
            throw new UnprocessableException(
                    "Boleto cannot be paid — current status: " + boleto.getStatus()
            );
        }

        boleto.markAsPaid();
        log.info("Boleto {} paid at {}", id, boleto.getPaidAt());
        return boletoMapper.toResponse(boletoRepository.save(boleto));
    }

    @Override
    @Transactional
    public BoletoResponse cancel(UUID id) {
        Boleto boleto = getBoletoOrThrow(id);

        if (!boleto.isPending()) {
            throw new UnprocessableException(
                    "Only PENDING boletos can be cancelled — current status: " + boleto.getStatus()
            );
        }

        boleto.cancel();
        log.info("Boleto {} cancelled", id);
        return boletoMapper.toResponse(boletoRepository.save(boleto));
    }

    // ── Private ────────────────────────────────────────────────

    private Boleto getBoletoOrThrow(UUID id) {
        return boletoRepository.findById(id)
                .orElseThrow(() -> new BoletoNotFoundException(id));
    }

    /**
     * Generates a simplified 47-digit bar code.
     * Real implementation follows FEBRABAN specification.
     */
    private String generateBarCode() {
        return String.format("0019%043d",
                ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L));
    }
}
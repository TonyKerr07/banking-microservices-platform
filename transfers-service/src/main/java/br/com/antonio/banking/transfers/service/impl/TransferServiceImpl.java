package br.com.antonio.banking.transfers.service.impl;

import br.com.antonio.banking.common.dto.PageResponse;
import br.com.antonio.banking.common.exception.UnprocessableException;
import br.com.antonio.banking.transfers.domain.entity.Transfer;
import br.com.antonio.banking.transfers.domain.enums.TransferStatus;
import br.com.antonio.banking.transfers.dto.request.CreateTransferRequest;
import br.com.antonio.banking.transfers.dto.response.TransferResponse;
import br.com.antonio.banking.transfers.exception.TransferNotFoundException;
import br.com.antonio.banking.transfers.mapper.TransferMapper;
import br.com.antonio.banking.transfers.repository.TransferRepository;
import br.com.antonio.banking.transfers.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    @Override
    @Transactional
    public TransferResponse create(CreateTransferRequest request) {
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new UnprocessableException("Source and target accounts must be different.");
        }

        log.info("Creating transfer from {} to {} — amount: {}",
                request.sourceAccountId(), request.targetAccountId(), request.amount());

        /*
         * NOTE: In a full implementation, this service would:
         * 1. Call accounts-service via RestClient to validate both accounts are ACTIVE
         * 2. Call accounts-service to debit source and credit target (or use a saga/outbox pattern)
         * 3. Persist the transfer with COMPLETED status
         *
         * For this skeleton, we persist directly as COMPLETED to keep focus on architecture.
         * The RestClient integration slot is reserved in the config package.
         */
        Transfer transfer = Transfer.builder()
                .sourceAccountId(request.sourceAccountId())
                .targetAccountId(request.targetAccountId())
                .amount(request.amount())
                .description(request.description())
                .transferType(request.transferType())
                .status(TransferStatus.COMPLETED)
                .build();

        Transfer saved = transferRepository.save(transfer);
        log.info("Transfer {} completed successfully", saved.getId());
        return transferMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse findById(UUID id) {
        return transferMapper.toResponse(
                transferRepository.findById(id).orElseThrow(() -> new TransferNotFoundException(id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferResponse> findAll(Pageable pageable) {
        return PageResponse.from(transferRepository.findAll(pageable), transferMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferResponse> findByAccount(UUID accountId, Pageable pageable) {
        return PageResponse.from(
                transferRepository.findAllByAccountId(accountId, pageable),
                transferMapper::toResponse
        );
    }
}
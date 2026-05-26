package br.com.antonio.banking.transfers.service.impl;

import br.com.antonio.banking.common.dto.PageResponse;
import br.com.antonio.banking.common.exception.ResourceNotFoundException;
import br.com.antonio.banking.common.exception.UnprocessableException;
import br.com.antonio.banking.transfers.client.AccountsClient;
import br.com.antonio.banking.transfers.client.dto.AccountInfo;
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
    private final AccountsClient accountsClient;

    @Override
    @Transactional
    public TransferResponse create(CreateTransferRequest request) {
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new UnprocessableException("Source and target accounts must be different.");
        }

        // ── Validate source account ──────────────────────────
        AccountInfo sourceAccount = accountsClient.findById(request.sourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "id", request.sourceAccountId()));

        if (!sourceAccount.isActive()) {
            throw new UnprocessableException(
                    "Source account is not active. Current status: " + sourceAccount.status());
        }

        // ── Validate target account ──────────────────────────
        AccountInfo targetAccount = accountsClient.findById(request.targetAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "id", request.targetAccountId()));

        if (!targetAccount.isActive()) {
            throw new UnprocessableException(
                    "Target account is not active. Current status: " + targetAccount.status());
        }

        log.info("Creating transfer from {} to {} — amount: {}",
                request.sourceAccountId(), request.targetAccountId(), request.amount());

        /*
         * NOTE: Debit/credit is not implemented here because it requires
         * a distributed transaction (saga pattern) or an outbox event.
         * This will be tackled in the Kafka/event-driven iteration.
         * For now, both accounts are validated and the transfer is persisted.
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
        log.info("Transfer {} completed: {} → {}, amount: {}",
                saved.getId(), request.sourceAccountId(), request.targetAccountId(), request.amount());
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
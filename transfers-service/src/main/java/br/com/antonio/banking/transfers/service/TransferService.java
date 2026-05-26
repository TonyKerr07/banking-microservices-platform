package br.com.antonio.banking.transfers.service;

import br.com.antonio.banking.transfers.dto.request.CreateTransferRequest;
import br.com.antonio.banking.transfers.dto.response.TransferResponse;
import br.com.antonio.banking.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransferService {
    TransferResponse create(CreateTransferRequest request);
    TransferResponse findById(UUID id);
    PageResponse<TransferResponse> findAll(Pageable pageable);
    PageResponse<TransferResponse> findByAccount(UUID accountId, Pageable pageable);
}
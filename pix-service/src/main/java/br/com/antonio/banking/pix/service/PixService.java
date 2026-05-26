package br.com.antonio.banking.pix.service;

import br.com.antonio.banking.common.dto.PageResponse;
import br.com.antonio.banking.pix.dto.request.CreatePixKeyRequest;
import br.com.antonio.banking.pix.dto.request.SendPixRequest;
import br.com.antonio.banking.pix.dto.response.PixKeyResponse;
import br.com.antonio.banking.pix.dto.response.PixTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PixService {

    PixKeyResponse registerKey(CreatePixKeyRequest request);

    List<PixKeyResponse> listKeysByAccount(UUID accountId);

    PixKeyResponse lookupKey(String keyValue);

    void deleteKey(UUID id);

    PixTransactionResponse sendPix(SendPixRequest request);

    PixTransactionResponse findTransactionById(UUID id);

    PageResponse<PixTransactionResponse> listTransactionsByAccount(UUID accountId, Pageable pageable);
}
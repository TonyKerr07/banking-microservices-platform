package br.com.antonio.banking.boletos.service;

import br.com.antonio.banking.boletos.dto.request.CreateBoletoRequest;
import br.com.antonio.banking.boletos.dto.response.BoletoResponse;
import br.com.antonio.banking.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BoletoService {
    BoletoResponse create(CreateBoletoRequest request);
    BoletoResponse findById(UUID id);
    PageResponse<BoletoResponse> findAll(Pageable pageable);
    BoletoResponse pay(UUID id);
    BoletoResponse cancel(UUID id);
}
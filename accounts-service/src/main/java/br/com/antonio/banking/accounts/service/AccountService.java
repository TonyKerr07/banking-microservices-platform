package br.com.antonio.banking.accounts.service;

import br.com.antonio.banking.accounts.dto.request.CreateAccountRequest;
import br.com.antonio.banking.accounts.dto.request.UpdateAccountStatusRequest;
import br.com.antonio.banking.accounts.dto.response.AccountResponse;
import br.com.antonio.banking.accounts.dto.response.BalanceResponse;
import br.com.antonio.banking.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AccountService {

    AccountResponse create(CreateAccountRequest request);

    AccountResponse findById(UUID id);

    PageResponse<AccountResponse> findAll(Pageable pageable);

    AccountResponse updateStatus(UUID id, UpdateAccountStatusRequest request);

    BalanceResponse getBalance(UUID id);
}
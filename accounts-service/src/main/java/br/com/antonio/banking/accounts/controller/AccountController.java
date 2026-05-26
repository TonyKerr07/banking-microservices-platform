package br.com.antonio.banking.accounts.controller;

import br.com.antonio.banking.accounts.dto.request.CreateAccountRequest;
import br.com.antonio.banking.accounts.dto.request.UpdateAccountStatusRequest;
import br.com.antonio.banking.accounts.dto.response.AccountResponse;
import br.com.antonio.banking.accounts.dto.response.BalanceResponse;
import br.com.antonio.banking.accounts.service.AccountService;
import br.com.antonio.banking.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for account operations.
 * Keeps it thin: validation + delegation to service layer only.
 *
 * Note on contract-first: in a full contract-first setup, this controller
 * would implement the generated AccountsApi interface.
 * For clarity in this skeleton, we wire it manually and implement the interface
 * in the next iteration.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<AccountResponse>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(accountService.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountStatusRequest request
    ) {
        return ResponseEntity.ok(accountService.updateStatus(id, request));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }
}
package br.com.antonio.banking.accounts.service.impl;

import br.com.antonio.banking.accounts.domain.entity.Account;
import br.com.antonio.banking.accounts.domain.enums.AccountStatus;
import br.com.antonio.banking.accounts.dto.request.CreateAccountRequest;
import br.com.antonio.banking.accounts.dto.request.UpdateAccountStatusRequest;
import br.com.antonio.banking.accounts.dto.response.AccountResponse;
import br.com.antonio.banking.accounts.dto.response.BalanceResponse;
import br.com.antonio.banking.accounts.exception.AccountNotFoundException;
import br.com.antonio.banking.accounts.mapper.AccountMapper;
import br.com.antonio.banking.accounts.repository.AccountRepository;
import br.com.antonio.banking.accounts.service.AccountService;
import br.com.antonio.banking.common.dto.PageResponse;
import br.com.antonio.banking.common.exception.ConflictException;
import br.com.antonio.banking.common.exception.UnprocessableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        log.info("Creating account for document: {}", maskDocument(request.documentNumber()));

        if (accountRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException(
                    "An account already exists for document number: " + maskDocument(request.documentNumber())
            );
        }

        Account account = Account.builder()
                .holderName(request.holderName())
                .documentNumber(request.documentNumber())
                .accountNumber(generateAccountNumber())
                .accountType(request.accountType())
                .status(AccountStatus.ACTIVE)
                .build();

        if (request.initialBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
            account.credit(request.initialBalance());
        }

        Account saved = accountRepository.save(account);
        log.info("Account created: id={}, number={}", saved.getId(), saved.getAccountNumber());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Cacheable(value = "account-data", key = "#id")
    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return accountMapper.toResponse(getAccountOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> findAll(Pageable pageable) {
        return PageResponse.from(accountRepository.findAll(pageable), accountMapper::toResponse);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "account-data",    key = "#id"),
            @CacheEvict(value = "account-balance", key = "#id")
    })
    @Transactional
    public AccountResponse updateStatus(UUID id, UpdateAccountStatusRequest request) {
        Account account = getAccountOrThrow(id);

        validateStatusTransition(account.getStatus(), request.status());

        account.setStatus(request.status());
        log.info("Account {} status changed from {} to {}", id, account.getStatus(), request.status());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Cacheable(value = "account-balance", key = "#id")
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID id) {
        Account account = getAccountOrThrow(id);
        if (account.isClosed()) {
            throw new UnprocessableException("Cannot query balance of a closed account.");
        }
        return accountMapper.toBalanceResponse(account);
    }

    // ── Private helpers ────────────────────────────────────────

    private Account getAccountOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void validateStatusTransition(AccountStatus current, AccountStatus target) {
        if (current == AccountStatus.CLOSED) {
            throw new UnprocessableException("A closed account cannot be reactivated.");
        }
        if (current == target) {
            throw new UnprocessableException("Account is already " + target.name().toLowerCase() + ".");
        }
    }

    private String generateAccountNumber() {
        // Format: YYYYMMDD + 6 random digits — simple for portfolio, real banks use sequences
        String datePart = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        );
        String randomPart = String.format("%06d", ThreadLocalRandom.current().nextInt(999999));
        return datePart + randomPart;
    }

    private String maskDocument(String document) {
        if (document == null || document.length() < 4) return "****";
        return "*".repeat(document.length() - 4) + document.substring(document.length() - 4);
    }
}
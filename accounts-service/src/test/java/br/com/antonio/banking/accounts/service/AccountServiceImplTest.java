package br.com.antonio.banking.accounts.service;

import br.com.antonio.banking.accounts.domain.entity.Account;
import br.com.antonio.banking.accounts.domain.enums.AccountStatus;
import br.com.antonio.banking.accounts.domain.enums.AccountType;
import br.com.antonio.banking.accounts.dto.request.CreateAccountRequest;
import br.com.antonio.banking.accounts.dto.request.UpdateAccountStatusRequest;
import br.com.antonio.banking.accounts.dto.response.AccountResponse;
import br.com.antonio.banking.accounts.exception.AccountNotFoundException;
import br.com.antonio.banking.accounts.mapper.AccountMapper;
import br.com.antonio.banking.accounts.repository.AccountRepository;
import br.com.antonio.banking.accounts.service.impl.AccountServiceImpl;
import br.com.antonio.banking.common.exception.ConflictException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl")
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account activeAccount;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        activeAccount = Account.builder()
                .id(UUID.randomUUID())
                .holderName("João da Silva")
                .documentNumber("12345678901")
                .accountNumber("202401010001")
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();

        accountResponse = new AccountResponse(
                activeAccount.getId(), activeAccount.getHolderName(),
                activeAccount.getDocumentNumber(), activeAccount.getAccountNumber(),
                activeAccount.getAccountType(), activeAccount.getStatus(),
                activeAccount.getBalance(), null, null
        );
    }

    @Nested
    @DisplayName("create()")
    class CreateAccount {

        @Test
        @DisplayName("should create account successfully when document is not registered")
        void shouldCreateAccountSuccessfully() {
            var request = new CreateAccountRequest(
                    "João da Silva", "12345678901", AccountType.CHECKING, BigDecimal.ZERO
            );

            when(accountRepository.existsByDocumentNumber("12345678901")).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);
            when(accountMapper.toResponse(activeAccount)).thenReturn(accountResponse);

            AccountResponse result = accountService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.holderName()).isEqualTo("João da Silva");
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("should throw ConflictException when document is already registered")
        void shouldThrowConflictWhenDocumentExists() {
            var request = new CreateAccountRequest(
                    "João da Silva", "12345678901", AccountType.CHECKING, BigDecimal.ZERO
            );

            when(accountRepository.existsByDocumentNumber("12345678901")).thenReturn(true);

            assertThatThrownBy(() -> accountService.create(request))
                    .isInstanceOf(ConflictException.class);

            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return account when found")
        void shouldReturnAccountWhenFound() {
            UUID id = activeAccount.getId();
            when(accountRepository.findById(id)).thenReturn(Optional.of(activeAccount));
            when(accountMapper.toResponse(activeAccount)).thenReturn(accountResponse);

            AccountResponse result = accountService.findById(id);

            assertThat(result.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when not found")
        void shouldThrowNotFoundWhenAccountMissing() {
            UUID id = UUID.randomUUID();
            when(accountRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.findById(id))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("should block an active account")
        void shouldBlockActiveAccount() {
            UUID id = activeAccount.getId();
            var request = new UpdateAccountStatusRequest(AccountStatus.BLOCKED);

            when(accountRepository.findById(id)).thenReturn(Optional.of(activeAccount));
            when(accountRepository.save(activeAccount)).thenReturn(activeAccount);
            when(accountMapper.toResponse(activeAccount)).thenReturn(accountResponse);

            accountService.updateStatus(id, request);

            verify(accountRepository).save(activeAccount);
        }

        @Test
        @DisplayName("should throw UnprocessableException when account is already closed")
        void shouldThrowWhenAccountIsClosed() {
            activeAccount.setStatus(AccountStatus.CLOSED);
            UUID id = activeAccount.getId();
            var request = new UpdateAccountStatusRequest(AccountStatus.ACTIVE);

            when(accountRepository.findById(id)).thenReturn(Optional.of(activeAccount));

            assertThatThrownBy(() -> accountService.updateStatus(id, request))
                    .isInstanceOf(UnprocessableException.class)
                    .hasMessageContaining("closed account");
        }
    }
}
package br.com.antonio.banking.accounts.exception;

import br.com.antonio.banking.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class AccountNotFoundException extends ResourceNotFoundException {

    public AccountNotFoundException(UUID id) {
        super("Account", "id", id);
    }

    public AccountNotFoundException(String accountNumber) {
        super("Account", "accountNumber", accountNumber);
    }
}
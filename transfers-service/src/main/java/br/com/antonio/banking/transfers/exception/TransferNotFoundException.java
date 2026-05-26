package br.com.antonio.banking.transfers.exception;

import br.com.antonio.banking.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class TransferNotFoundException extends ResourceNotFoundException {
    public TransferNotFoundException(UUID id) {
        super("Transfer", "id", id);
    }
}
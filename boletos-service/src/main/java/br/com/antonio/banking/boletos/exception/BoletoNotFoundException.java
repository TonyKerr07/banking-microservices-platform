package br.com.antonio.banking.boletos.exception;

import br.com.antonio.banking.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class BoletoNotFoundException extends ResourceNotFoundException {
    public BoletoNotFoundException(UUID id) {
        super("Boleto", "id", id);
    }
}
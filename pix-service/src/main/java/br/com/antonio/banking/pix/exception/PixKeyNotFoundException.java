package br.com.antonio.banking.pix.exception;

import br.com.antonio.banking.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class PixKeyNotFoundException extends ResourceNotFoundException {

    public PixKeyNotFoundException(UUID id) {
        super("PixKey", "id", id);
    }

    public PixKeyNotFoundException(String keyValue) {
        super("PixKey", "keyValue", keyValue);
    }
}
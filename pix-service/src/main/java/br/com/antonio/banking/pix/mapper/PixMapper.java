package br.com.antonio.banking.pix.mapper;

import br.com.antonio.banking.pix.domain.entity.PixKey;
import br.com.antonio.banking.pix.domain.entity.PixTransaction;
import br.com.antonio.banking.pix.dto.response.PixKeyResponse;
import br.com.antonio.banking.pix.dto.response.PixTransactionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PixMapper {
    PixKeyResponse toKeyResponse(PixKey pixKey);
    PixTransactionResponse toTransactionResponse(PixTransaction transaction);
}
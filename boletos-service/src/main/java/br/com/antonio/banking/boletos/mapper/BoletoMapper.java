package br.com.antonio.banking.boletos.mapper;

import br.com.antonio.banking.boletos.domain.entity.Boleto;
import br.com.antonio.banking.boletos.dto.response.BoletoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoletoMapper {
    BoletoResponse toResponse(Boleto boleto);
}
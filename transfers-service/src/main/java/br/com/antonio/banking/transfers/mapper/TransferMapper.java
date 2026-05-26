package br.com.antonio.banking.transfers.mapper;

import br.com.antonio.banking.transfers.domain.entity.Transfer;
import br.com.antonio.banking.transfers.dto.response.TransferResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    TransferResponse toResponse(Transfer transfer);
}
package br.com.antonio.banking.accounts.mapper;

import br.com.antonio.banking.accounts.domain.entity.Account;
import br.com.antonio.banking.accounts.dto.response.AccountResponse;
import br.com.antonio.banking.accounts.dto.response.BalanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

/**
 * MapStruct mapper — zero-reflection at runtime, generated at compile time.
 * componentModel = "spring" → Spring injeta como @Bean automaticamente.
 */
@Mapper(componentModel = "spring", imports = Instant.class)
public interface AccountMapper {

    AccountResponse toResponse(Account account);

    @Mapping(target = "accountId", source = "id")
    @Mapping(target = "consultedAt", expression = "java(Instant.now())")
    BalanceResponse toBalanceResponse(Account account);
}
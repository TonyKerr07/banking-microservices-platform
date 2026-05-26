package br.com.antonio.banking.transfers.domain.enums;

public enum TransferType {
    INTERNAL,   // Entre contas da mesma plataforma
    TED,        // Transferência entre bancos (mesmo dia)
    DOC         // Transferência entre bancos (D+1, legado)
}
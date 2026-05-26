package br.com.antonio.banking.boletos.repository;

import br.com.antonio.banking.boletos.domain.entity.Boleto;
import br.com.antonio.banking.boletos.domain.enums.BoletoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BoletoRepository extends JpaRepository<Boleto, UUID> {

    Page<Boleto> findByPayerAccountId(UUID payerAccountId, Pageable pageable);

    Page<Boleto> findByStatus(BoletoStatus status, Pageable pageable);

    // Útil para job de atualização de boletos vencidos
    List<Boleto> findByStatusAndDueDateBefore(BoletoStatus status, LocalDate date);
}
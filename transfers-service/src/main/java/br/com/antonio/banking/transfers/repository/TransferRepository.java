package br.com.antonio.banking.transfers.repository;

import br.com.antonio.banking.transfers.domain.entity.Transfer;
import br.com.antonio.banking.transfers.domain.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    @Query("""
        SELECT t FROM Transfer t
        WHERE t.sourceAccountId = :accountId OR t.targetAccountId = :accountId
        ORDER BY t.createdAt DESC
        """)
    Page<Transfer> findAllByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    Page<Transfer> findByStatus(TransferStatus status, Pageable pageable);
}
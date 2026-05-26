package br.com.antonio.banking.pix.repository;

import br.com.antonio.banking.pix.domain.entity.PixTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PixTransactionRepository extends JpaRepository<PixTransaction, UUID> {

    @Query("""
        SELECT t FROM PixTransaction t
        WHERE t.sourceAccountId = :accountId OR t.targetAccountId = :accountId
        ORDER BY t.createdAt DESC
        """)
    Page<PixTransaction> findAllByAccountId(@Param("accountId") UUID accountId, Pageable pageable);
}
package br.com.antonio.banking.accounts.repository;

import br.com.antonio.banking.accounts.domain.entity.Account;
import br.com.antonio.banking.accounts.domain.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByDocumentNumber(String documentNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.documentNumber = :doc AND a.status <> 'CLOSED'")
    Optional<Account> findActiveByDocumentNumber(@Param("doc") String documentNumber);
}
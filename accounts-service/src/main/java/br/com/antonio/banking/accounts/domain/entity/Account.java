package br.com.antonio.banking.accounts.domain.entity;

import br.com.antonio.banking.accounts.domain.enums.AccountStatus;
import br.com.antonio.banking.accounts.domain.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Core domain entity for a bank account.
 *
 * Design decisions:
 * - UUID as PK: avoids sequential ID exposure and works well in distributed systems
 * - BigDecimal for balance: precision is mandatory for financial values — never use double
 * - accountNumber: business-facing identifier (formatted), separate from internal UUID
 * - @Version for optimistic locking: prevents lost update in concurrent balance operations
 */
@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(name = "idx_accounts_document_number", columnList = "document_number"),
                @Index(name = "idx_accounts_account_number", columnList = "account_number", unique = true),
                @Index(name = "idx_accounts_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "holder_name", nullable = false, length = 150)
    private String holderName;

    @Column(name = "document_number", nullable = false, length = 14)
    private String documentNumber;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Domain methods ─────────────────────────────────────────

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }

    public boolean isClosed() {
        return AccountStatus.CLOSED.equals(this.status);
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }
}
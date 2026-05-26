package br.com.antonio.banking.pix.domain.entity;

import br.com.antonio.banking.pix.domain.enums.PixTransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Records every PIX payment attempt.
 *
 * Design notes:
 * - endToEndId follows BACEN E2EID format (simplified here as UUID).
 * - targetAccountId is resolved at runtime via PIX key lookup.
 * - failureReason stored for auditing — required by BACEN.
 */
@Entity
@Table(
        name = "pix_transactions",
        indexes = {
                @Index(name = "idx_pix_tx_source_account", columnList = "source_account_id"),
                @Index(name = "idx_pix_tx_target_account", columnList = "target_account_id"),
                @Index(name = "idx_pix_tx_end_to_end_id", columnList = "end_to_end_id", unique = true),
                @Index(name = "idx_pix_tx_status", columnList = "status"),
                @Index(name = "idx_pix_tx_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "target_pix_key", nullable = false, length = 77)
    private String targetPixKey;

    @Column(name = "target_account_id")
    private UUID targetAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 140)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PixTransactionStatus status = PixTransactionStatus.PENDING;

    @Column(name = "end_to_end_id", nullable = false, unique = true, length = 36)
    private String endToEndId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
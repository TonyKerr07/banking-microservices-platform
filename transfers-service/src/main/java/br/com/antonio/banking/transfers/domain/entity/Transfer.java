package br.com.antonio.banking.transfers.domain.entity;

import br.com.antonio.banking.transfers.domain.enums.TransferStatus;
import br.com.antonio.banking.transfers.domain.enums.TransferType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transfer entity.
 *
 * Design notes:
 * - sourceAccountId / targetAccountId são UUIDs de referência — sem FK para o
 *   accounts-service (cada serviço é dono do seu próprio banco de dados).
 * - BigDecimal para valor monetário — nunca double/float em financeiro.
 * - status + failureReason permitem rastrear falhas sem perder o registro.
 */
@Entity
@Table(
        name = "transfers",
        indexes = {
                @Index(name = "idx_transfers_source_account", columnList = "source_account_id"),
                @Index(name = "idx_transfers_target_account", columnList = "target_account_id"),
                @Index(name = "idx_transfers_status", columnList = "status"),
                @Index(name = "idx_transfers_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "target_account_id", nullable = false)
    private UUID targetAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 20)
    @Builder.Default
    private TransferType transferType = TransferType.INTERNAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransferStatus status = TransferStatus.PENDING;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
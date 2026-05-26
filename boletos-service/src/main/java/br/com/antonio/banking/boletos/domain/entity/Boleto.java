package br.com.antonio.banking.boletos.domain.entity;

import br.com.antonio.banking.boletos.domain.enums.BoletoStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "boletos",
        indexes = {
                @Index(name = "idx_boletos_payer_account", columnList = "payer_account_id"),
                @Index(name = "idx_boletos_status", columnList = "status"),
                @Index(name = "idx_boletos_due_date", columnList = "due_date"),
                @Index(name = "idx_boletos_bar_code", columnList = "bar_code", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boleto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "bar_code", nullable = false, unique = true, length = 48)
    private String barCode;

    @Column(name = "payer_account_id", nullable = false)
    private UUID payerAccountId;

    @Column(name = "beneficiary_name", nullable = false, length = 150)
    private String beneficiaryName;

    @Column(name = "beneficiary_document", length = 14)
    private String beneficiaryDocument;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "description", length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BoletoStatus status = BoletoStatus.PENDING;

    @Column(name = "paid_at")
    private Instant paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Domain methods ─────────────────────────────────────────

    public boolean isPending() {
        return BoletoStatus.PENDING.equals(this.status);
    }

    public void markAsPaid() {
        this.status = BoletoStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void cancel() {
        this.status = BoletoStatus.CANCELLED;
    }
}
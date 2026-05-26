package br.com.antonio.banking.pix.domain.entity;

import br.com.antonio.banking.pix.domain.enums.PixKeyStatus;
import br.com.antonio.banking.pix.domain.enums.PixKeyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a registered PIX key.
 *
 * Design notes:
 * - A single account can have at most 5 PIX keys (BACEN rule) — enforced at service layer.
 * - keyValue is unique across the system (no two accounts can share the same key).
 * - RANDOM keys are UUID-generated server-side when keyValue is blank.
 * - Soft delete via status = DELETED (BACEN requires 90-day retention after deactivation).
 */
@Entity
@Table(
        name = "pix_keys",
        indexes = {
                @Index(name = "idx_pix_keys_account_id", columnList = "account_id"),
                @Index(name = "idx_pix_keys_key_value", columnList = "key_value", unique = true),
                @Index(name = "idx_pix_keys_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 20)
    private PixKeyType keyType;

    @Column(name = "key_value", nullable = false, unique = true, length = 77)
    private String keyValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PixKeyStatus status = PixKeyStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ── Domain methods ─────────────────────────────────────────

    public boolean isActive() {
        return PixKeyStatus.ACTIVE.equals(this.status);
    }

    public void delete() {
        this.status = PixKeyStatus.DELETED;
        this.deletedAt = Instant.now();
    }
}
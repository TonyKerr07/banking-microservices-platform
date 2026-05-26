-- ================================================================
-- V1: Create pix_keys and pix_transactions tables
-- ================================================================

CREATE TABLE IF NOT EXISTS pix_keys (
                                        id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    account_id  UUID        NOT NULL,
    key_type    VARCHAR(20) NOT NULL,
    key_value   VARCHAR(77) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT pk_pix_keys PRIMARY KEY (id),
    CONSTRAINT uq_pix_keys_key_value UNIQUE (key_value),
    CONSTRAINT chk_pix_keys_type
    CHECK (key_type IN ('CPF', 'CNPJ', 'EMAIL', 'PHONE', 'RANDOM')),
    CONSTRAINT chk_pix_keys_status
    CHECK (status IN ('ACTIVE', 'DELETED'))
    );

CREATE INDEX IF NOT EXISTS idx_pix_keys_account_id ON pix_keys (account_id);
CREATE INDEX IF NOT EXISTS idx_pix_keys_key_value  ON pix_keys (key_value);
CREATE INDEX IF NOT EXISTS idx_pix_keys_status     ON pix_keys (status);

-- ──────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS pix_transactions (
                                                id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    source_account_id   UUID            NOT NULL,
    target_pix_key      VARCHAR(77)     NOT NULL,
    target_account_id   UUID,
    amount              NUMERIC(19, 2)  NOT NULL,
    description         VARCHAR(140),
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    end_to_end_id       VARCHAR(36)     NOT NULL,
    failure_reason      VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_pix_transactions PRIMARY KEY (id),
    CONSTRAINT uq_pix_tx_end_to_end_id UNIQUE (end_to_end_id),
    CONSTRAINT chk_pix_tx_amount CHECK (amount > 0),
    CONSTRAINT chk_pix_tx_status
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
    );

CREATE INDEX IF NOT EXISTS idx_pix_tx_source_account ON pix_transactions (source_account_id);
CREATE INDEX IF NOT EXISTS idx_pix_tx_target_account ON pix_transactions (target_account_id);
CREATE INDEX IF NOT EXISTS idx_pix_tx_status         ON pix_transactions (status);
CREATE INDEX IF NOT EXISTS idx_pix_tx_created_at     ON pix_transactions (created_at DESC);
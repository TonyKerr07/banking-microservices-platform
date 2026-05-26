CREATE TABLE IF NOT EXISTS transfers (
                                         id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    source_account_id   UUID            NOT NULL,
    target_account_id   UUID            NOT NULL,
    amount              NUMERIC(19, 2)  NOT NULL,
    description         VARCHAR(200),
    transfer_type       VARCHAR(20)     NOT NULL DEFAULT 'INTERNAL',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    failure_reason      VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_transfers PRIMARY KEY (id),
    CONSTRAINT chk_transfers_amount CHECK (amount > 0),
    CONSTRAINT chk_transfers_different_accounts
    CHECK (source_account_id <> target_account_id),
    CONSTRAINT chk_transfers_type
    CHECK (transfer_type IN ('INTERNAL', 'TED', 'DOC')),
    CONSTRAINT chk_transfers_status
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED'))
    );

CREATE INDEX IF NOT EXISTS idx_transfers_source_account ON transfers (source_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_target_account ON transfers (target_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_status         ON transfers (status);
CREATE INDEX IF NOT EXISTS idx_transfers_created_at     ON transfers (created_at DESC);
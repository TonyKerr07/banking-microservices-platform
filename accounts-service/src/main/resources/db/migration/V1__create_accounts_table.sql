-- ================================================================
-- V1: Create accounts table
-- ================================================================

CREATE TABLE IF NOT EXISTS accounts (
                                        id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    holder_name     VARCHAR(150)    NOT NULL,
    document_number VARCHAR(14)     NOT NULL,
    account_number  VARCHAR(20)     NOT NULL,
    account_type    VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    balance         NUMERIC(19, 2)  NOT NULL DEFAULT 0.00,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    CONSTRAINT chk_accounts_balance CHECK (balance >= 0),
    CONSTRAINT chk_accounts_type CHECK (account_type IN ('CHECKING', 'SAVINGS', 'SALARY')),
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
    );

CREATE INDEX IF NOT EXISTS idx_accounts_document_number ON accounts (document_number);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts (status);
CREATE INDEX IF NOT EXISTS idx_accounts_created_at ON accounts (created_at DESC);
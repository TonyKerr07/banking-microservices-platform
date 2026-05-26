CREATE TABLE IF NOT EXISTS boletos (
                                       id                      UUID            NOT NULL DEFAULT gen_random_uuid(),
    bar_code                VARCHAR(48)     NOT NULL,
    payer_account_id        UUID            NOT NULL,
    beneficiary_name        VARCHAR(150)    NOT NULL,
    beneficiary_document    VARCHAR(14),
    amount                  NUMERIC(19, 2)  NOT NULL,
    due_date                DATE            NOT NULL,
    description             VARCHAR(200),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    paid_at                 TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_boletos PRIMARY KEY (id),
    CONSTRAINT uq_boletos_bar_code UNIQUE (bar_code),
    CONSTRAINT chk_boletos_amount CHECK (amount > 0),
    CONSTRAINT chk_boletos_status CHECK (status IN ('PENDING', 'PAID', 'CANCELLED', 'OVERDUE'))
    );

CREATE INDEX IF NOT EXISTS idx_boletos_payer_account ON boletos (payer_account_id);
CREATE INDEX IF NOT EXISTS idx_boletos_status        ON boletos (status);
CREATE INDEX IF NOT EXISTS idx_boletos_due_date      ON boletos (due_date);
CREATE TABLE idempotency_records (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    idempotency_key VARCHAR(128) NOT NULL,

    request_hash VARCHAR(64) NOT NULL,

    expense_id BIGINT,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_idempotency_user_key
        UNIQUE (user_id, idempotency_key),

    CONSTRAINT fk_idempotency_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_idempotency_expense
        FOREIGN KEY (expense_id)
        REFERENCES expenses(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_idempotency_created_at
    ON idempotency_records(created_at);

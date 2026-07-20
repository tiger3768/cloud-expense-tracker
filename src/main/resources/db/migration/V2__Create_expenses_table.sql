CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(255) NOT NULL,

    amount NUMERIC(12,2) NOT NULL,

    category VARCHAR(50) NOT NULL,

    description TEXT,

    expense_date DATE NOT NULL,

    receipt_url VARCHAR(500),

    created_at TIMESTAMP NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT fk_expense_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
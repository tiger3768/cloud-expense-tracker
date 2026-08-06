ALTER TABLE expenses
    ADD COLUMN updated_at TIMESTAMP,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN updated_by BIGINT;

UPDATE expenses
SET created_by = user_id
WHERE created_by IS NULL;

UPDATE expenses
SET updated_by = user_id
WHERE updated_by IS NULL;

UPDATE expenses
SET updated_at = created_at
WHERE updated_at IS NULL;


ALTER TABLE expenses
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE expenses
    ADD CONSTRAINT fk_expense_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_expense_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;


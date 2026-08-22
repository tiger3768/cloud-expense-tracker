ALTER TABLE expenses
    ADD COLUMN deleted_at TIMESTAMP;

-- Backfill any legacy audit rows that were created before auditing
-- was correctly populated.
UPDATE expenses
SET created_by = user_id
WHERE created_by IS NULL;

UPDATE expenses
SET updated_by = user_id
WHERE updated_by IS NULL;

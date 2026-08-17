-- Keep only the newest verification token for each user before adding the uniqueness constraint.
DELETE FROM email_verification_tokens older
USING email_verification_tokens newer
WHERE older.user_id = newer.user_id
  AND older.id < newer.id;

-- Keep only the newest password reset token for each user before adding the uniqueness constraint.
DELETE FROM password_reset_tokens older
USING password_reset_tokens newer
WHERE older.user_id = newer.user_id
  AND older.id < newer.id;

ALTER TABLE email_verification_tokens
    ADD CONSTRAINT uq_email_verification_tokens_user
    UNIQUE (user_id);

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT uq_password_reset_tokens_user
    UNIQUE (user_id);

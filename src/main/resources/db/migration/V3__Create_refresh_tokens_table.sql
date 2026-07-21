CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,

    token VARCHAR(255) NOT NULL UNIQUE,

    expires_at TIMESTAMP NOT NULL,

    revoked BOOLEAN NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token
    ON refresh_tokens(token);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);
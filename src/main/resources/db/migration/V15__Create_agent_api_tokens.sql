CREATE TABLE agent_api_tokens (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    token_prefix VARCHAR(12) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL,

    last_used_at TIMESTAMP,

    CONSTRAINT fk_agent_api_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_agent_api_tokens_user_id
    ON agent_api_tokens(user_id);

CREATE INDEX idx_agent_api_tokens_expiry
    ON agent_api_tokens(expires_at);

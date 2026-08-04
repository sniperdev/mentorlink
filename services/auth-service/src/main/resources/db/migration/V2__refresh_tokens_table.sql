CREATE TABLE refresh_tokens
(
    id           UUID PRIMARY KEY,
    user_id      UUID        NOT NULL,
    token_hash   VARCHAR(64) NOT NULL UNIQUE,
    expires_at   TIMESTAMP   NOT NULL,
    revoked      BOOLEAN     NOT NULL DEFAULT FALSE,
    last_used_at TIMESTAMP   NULL,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP   NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);
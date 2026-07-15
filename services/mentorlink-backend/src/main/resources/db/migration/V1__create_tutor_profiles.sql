CREATE TABLE tutor_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    bio TEXT,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
)
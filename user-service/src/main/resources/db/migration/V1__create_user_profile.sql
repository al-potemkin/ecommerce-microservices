CREATE TABLE user_profile
(
    id               BIGSERIAL PRIMARY KEY,
    keycloak_user_id UUID        NOT NULL UNIQUE,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    email            VARCHAR(255),
    phone            VARCHAR(20),
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    version          BIGINT      NOT NULL DEFAULT 0
);
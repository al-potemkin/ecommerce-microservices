CREATE TABLE user_address
(
    profile_id   UUID PRIMARY KEY REFERENCES user_profile (id) ON DELETE CASCADE,
    city         VARCHAR(100),
    address_line VARCHAR(255),
    postal_code  VARCHAR(10),
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    version      BIGINT      NOT NULL DEFAULT 0
);
CREATE TABLE user_address
(
    id             BIGSERIAL PRIMARY KEY,
    profile_id     BIGINT       NOT NULL REFERENCES user_profile (id) ON DELETE CASCADE,
    recipient_name VARCHAR(200) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    city           VARCHAR(100) NOT NULL,
    address_line   VARCHAR(255) NOT NULL,
    postal_code    VARCHAR(10),
    is_default     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_address_profile ON user_address (profile_id);

-- at most one default address per profile
CREATE UNIQUE INDEX uq_user_address_default ON user_address (profile_id) WHERE is_default;
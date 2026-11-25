CREATE TABLE outbox
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id     VARCHAR(36) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    type         VARCHAR(32) NOT NULL,
    payload      TEXT        NOT NULL,
    status       VARCHAR(16) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    sent_at      TIMESTAMPTZ,
    attempts     INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_pending ON outbox (id) WHERE status = 'PENDING';
CREATE TABLE reservation (
                             id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             order_id      VARCHAR(64)  NOT NULL,
                             stock_item_id BIGINT       NOT NULL REFERENCES stock_item (id),
                             quantity      INTEGER      NOT NULL,
                             status        VARCHAR(16)  NOT NULL,
                             expires_at    TIMESTAMPTZ  NOT NULL,
                             version       BIGINT       NOT NULL DEFAULT 0,
                             created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                             updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

                             CONSTRAINT uq_reservation_order_stock UNIQUE (order_id, stock_item_id),
                             CONSTRAINT chk_reservation_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_reservation_stock_item ON reservation (stock_item_id);
CREATE INDEX idx_reservation_expiry ON reservation (expires_at) WHERE status = 'RESERVED';
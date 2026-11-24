CREATE TABLE orders (
                        id                  UUID            PRIMARY KEY,
                        user_id             UUID            NOT NULL,
                        status              VARCHAR(32)     NOT NULL,
                        total_amount        NUMERIC(12, 2)  NOT NULL,
                        recipient_name      VARCHAR(200)    NOT NULL,
                        recipient_phone     VARCHAR(32)     NOT NULL,
                        delivery_city       VARCHAR(100)    NOT NULL,
                        delivery_address    VARCHAR(300)    NOT NULL,
                        expires_at          TIMESTAMPTZ     NOT NULL,
                        paid_at             TIMESTAMPTZ,
                        shipped_at          TIMESTAMPTZ,
                        return_requested_at TIMESTAMPTZ,
                        finished_at         TIMESTAMPTZ,
                        version             BIGINT          NOT NULL,
                        created_at          TIMESTAMPTZ     NOT NULL,
                        updated_at          TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);

CREATE TABLE order_item (
                            id           BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            order_id     UUID            NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
                            sku          VARCHAR(64)     NOT NULL,
                            product_id   VARCHAR(64)     NOT NULL,
                            product_name VARCHAR(255)    NOT NULL,
                            volume_ml    INTEGER         NOT NULL,
                            unit_price   NUMERIC(12, 2)  NOT NULL,
                            quantity     INTEGER         NOT NULL CHECK (quantity > 0),
                            CONSTRAINT uq_order_item_order_sku UNIQUE (order_id, sku)
);
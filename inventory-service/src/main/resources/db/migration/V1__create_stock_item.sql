CREATE TABLE stock_item (
                            id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            sku         VARCHAR(64)  NOT NULL,
                            product_id  VARCHAR(64)  NOT NULL,
                            on_hand     INTEGER      NOT NULL DEFAULT 0,
                            reserved    INTEGER      NOT NULL DEFAULT 0,
                            version     BIGINT       NOT NULL DEFAULT 0,
                            created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
                            updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

                            CONSTRAINT uq_stock_item_sku UNIQUE (sku),
                            CONSTRAINT chk_stock_item_on_hand_non_negative CHECK (on_hand >= 0),
                            CONSTRAINT chk_stock_item_reserved_non_negative CHECK (reserved >= 0),
                            CONSTRAINT chk_stock_item_reserved_not_above_on_hand CHECK (reserved <= on_hand)
);

CREATE INDEX idx_stock_item_product_id ON stock_item (product_id);
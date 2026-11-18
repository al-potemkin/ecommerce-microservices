CREATE TABLE processed_order_id
(
    order_id     UUID PRIMARY KEY,
    processed_at TIMESTAMP DEFAULT NOW()
);
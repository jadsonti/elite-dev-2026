CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    total_price NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,

    CONSTRAINT fk_reservations_event
        FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_reservations_customer
        FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT ck_reservations_quantity CHECK (quantity > 0),
    CONSTRAINT ck_reservations_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_reservations_total_price CHECK (total_price >= 0),
    CONSTRAINT ck_reservations_status
        CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED'))
);

CREATE INDEX idx_reservations_customer_created_at
    ON reservations(customer_id, created_at DESC);

CREATE INDEX idx_reservations_event
    ON reservations(event_id);

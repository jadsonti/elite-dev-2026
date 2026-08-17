CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_payments_customer
        FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT ck_payments_amount CHECK (amount >= 0),
    CONSTRAINT ck_payments_status CHECK (status IN ('APPROVED', 'DECLINED')),
    CONSTRAINT ck_payments_failure_reason CHECK (
        (status = 'APPROVED' AND failure_reason IS NULL)
        OR (status = 'DECLINED' AND failure_reason IS NOT NULL)
    )
);

CREATE INDEX idx_payments_reservation_processed_at
    ON payments(reservation_id, processed_at DESC);

CREATE INDEX idx_payments_customer
    ON payments(customer_id);

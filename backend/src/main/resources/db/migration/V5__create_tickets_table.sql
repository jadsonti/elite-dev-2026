CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    sequence_number INTEGER NOT NULL,
    code UUID NOT NULL,
    signature VARCHAR(43) NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,

    CONSTRAINT fk_tickets_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_tickets_customer
        FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT uk_tickets_code UNIQUE (code),
    CONSTRAINT uk_tickets_reservation_sequence
        UNIQUE (reservation_id, sequence_number),
    CONSTRAINT ck_tickets_sequence CHECK (sequence_number > 0),
    CONSTRAINT ck_tickets_status CHECK (status IN ('ACTIVE', 'USED')),
    CONSTRAINT ck_tickets_used_at CHECK (
        (status = 'ACTIVE' AND used_at IS NULL)
        OR (status = 'USED' AND used_at IS NOT NULL)
    )
);

CREATE INDEX idx_tickets_customer_issued_at
    ON tickets(customer_id, issued_at DESC);

CREATE INDEX idx_tickets_reservation
    ON tickets(reservation_id);

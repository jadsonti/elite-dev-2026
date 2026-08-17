CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,

    external_source VARCHAR(30),
    external_id VARCHAR(100),

    title VARCHAR(180) NOT NULL,
    description TEXT,
    image_url VARCHAR(1000),

    event_date_time TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,

    capacity INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL,

    price NUMERIC(12, 2) NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_by BIGINT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_events_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id),

    CONSTRAINT ck_events_capacity
        CHECK (capacity > 0),

    CONSTRAINT ck_events_available_quantity
        CHECK (
            available_quantity >= 0
            AND available_quantity <= capacity
        ),

    CONSTRAINT ck_events_price
        CHECK (price >= 0),

    CONSTRAINT ck_events_status
        CHECK (
            status IN (
                'DRAFT',
                'PUBLISHED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_events_status
    ON events(status);

CREATE INDEX idx_events_event_date_time
    ON events(event_date_time);

CREATE INDEX idx_events_created_by
    ON events(created_by);
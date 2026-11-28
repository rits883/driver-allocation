-- Immutable event identity and payload, stored in the allocation transaction.
CREATE TABLE allocation_outbox (
    event_id UUID PRIMARY KEY,
    order_reference VARCHAR(100) NOT NULL UNIQUE REFERENCES orders(order_reference),
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ
);
CREATE INDEX idx_allocation_outbox_pending
    ON allocation_outbox (created_at, event_id) WHERE published_at IS NULL;

CREATE TABLE allocation_inbox (
    consumer_name VARCHAR(100) NOT NULL,
    event_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (consumer_name, event_id)
);

-- Each logical consumer gets an independent projection, with one outcome per order.
CREATE TABLE allocation_event_projection (
    consumer_name VARCHAR(100) NOT NULL,
    order_reference VARCHAR(100) NOT NULL,
    event_id UUID NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    driver_id BIGINT,
    available_at BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (consumer_name, order_reference),
    UNIQUE (consumer_name, event_id),
    CHECK ((event_type = 'DRIVER_ALLOCATED' AND driver_id IS NOT NULL AND available_at IS NOT NULL)
        OR (event_type = 'ALLOCATION_REJECTED' AND driver_id IS NULL AND available_at IS NULL))
);

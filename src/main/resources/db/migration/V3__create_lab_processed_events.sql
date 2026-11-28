CREATE TABLE lab_processed_events (
    consumer_name VARCHAR(100) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (consumer_name, event_id)
);

CREATE TABLE lab_driver_delivery_stats (
    driver_id VARCHAR(100) PRIMARY KEY,
    completed_deliveries BIGINT NOT NULL DEFAULT 0
);

INSERT INTO lab_driver_delivery_stats (
    driver_id,
    completed_deliveries
)
VALUES ('D101', 0);

-- V2: Coordination for persistent driver allocation

CREATE TABLE allocation_coordination (
    id SMALLINT PRIMARY KEY,
    last_order_time BIGINT NOT NULL DEFAULT -1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_allocation_coordination_id
        CHECK (id = 1),

    CONSTRAINT chk_last_order_time
        CHECK (last_order_time >= -1)
);

-- One coordination row for the current allocation pool.
INSERT INTO allocation_coordination (
    id,
    last_order_time
)
VALUES (1, -1);

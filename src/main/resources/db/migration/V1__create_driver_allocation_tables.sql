
-- V1: Initial driver allocation schema

CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    driver_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    available_at BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_driver_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_reference VARCHAR(100) NOT NULL UNIQUE,
    order_time BIGINT NOT NULL,
    travel_time BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_order_time CHECK (order_time >= 0),
    CONSTRAINT chk_travel_time CHECK (travel_time > 0),
    CONSTRAINT chk_order_status
        CHECK (status IN ('PENDING', 'ASSIGNED', 'REJECTED'))
);

CREATE TABLE driver_allocations (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    driver_id BIGINT NOT NULL,
    allocated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_allocation_order
        FOREIGN KEY (order_id) REFERENCES orders(id),

    CONSTRAINT fk_allocation_driver
        FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

CREATE INDEX idx_drivers_status_available
    ON drivers (status, available_at, id);

CREATE INDEX idx_allocations_driver
    ON driver_allocations (driver_id);

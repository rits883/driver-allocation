package com.loginext.challenge.driverAllocation.kafka.events;

import java.time.Instant;
import java.util.UUID;

public record AllocationOutcomeEvent(
        UUID eventId, int schemaVersion, String eventType,
        String orderReference, long orderTime, long travelTime,
        Long driverId, Long availableAt, String occurredAt) {

    public AllocationOutcomeEvent {
        if (eventId == null || schemaVersion != 1 || orderReference == null
                || orderReference.isBlank() || orderReference.length() > 100
                || orderTime < 0 || travelTime <= 0 || occurredAt == null) {
            throw new IllegalArgumentException("Invalid allocation event");
        }
        Instant.parse(occurredAt);
        if ("DRIVER_ALLOCATED".equals(eventType)) {
            if (driverId == null || driverId <= 0 || availableAt == null
                    || availableAt != Math.addExact(orderTime, travelTime)) {
                throw new IllegalArgumentException("Invalid assigned outcome");
            }
        } else if (!"ALLOCATION_REJECTED".equals(eventType)
                || driverId != null || availableAt != null) {
            throw new IllegalArgumentException("Invalid rejected outcome");
        }
    }

    public static AllocationOutcomeEvent create(String reference, long orderTime,
            long travelTime, Long driverId, Long availableAt) {
        return new AllocationOutcomeEvent(UUID.randomUUID(), 1,
                driverId == null ? "ALLOCATION_REJECTED" : "DRIVER_ALLOCATED",
                reference, orderTime, travelTime, driverId, availableAt,
                Instant.now().toString());
    }
}

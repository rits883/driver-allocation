
package com.loginext.challenge.driverAllocation.domain.engine;

import java.util.Arrays;

public final class DriverAllocationEngine {

    private final long[] availableAt;

    public DriverAllocationEngine(int driverCount) {
        if (driverCount <= 0) {
            throw new IllegalArgumentException(
                    "Driver count must be positive");
        }

        this.availableAt = new long[driverCount];
    }

    public int allocate(long orderTime, long travelTime) {

        if (orderTime < 0) {
            throw new IllegalArgumentException(
                    "Order time cannot be negative");
        }

        if (travelTime <= 0) {
            throw new IllegalArgumentException(
                    "Travel time must be positive");
        }


        for (int i = 0; i < availableAt.length; i++) {

            if (availableAt[i] <= orderTime) {

                long nextAvailableAt =
                        Math.addExact(orderTime, travelTime);

                availableAt[i] = nextAvailableAt;

                return i + 1;
            }
        }

        return -1;
    }

    public long[] availabilitySnapshot() {
        return Arrays.copyOf(
                availableAt, availableAt.length);
    }
}

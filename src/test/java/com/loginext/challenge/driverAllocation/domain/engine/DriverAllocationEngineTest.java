
package com.loginext.challenge.driverAllocation.domain.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DriverAllocationEngineTest {

    @Test
    void shouldMatchLogiNextSample() {

        DriverAllocationEngine engine =
                new DriverAllocationEngine(2);

        assertEquals(1, engine.allocate(1, 10));
        assertEquals(2, engine.allocate(4, 20));
        assertEquals(1, engine.allocate(15, 5));
        assertEquals(1, engine.allocate(22, 20));
        assertEquals(2, engine.allocate(24, 10));
        assertEquals(-1, engine.allocate(25, 10));
    }

    @Test
    void shouldReuseDriverAtExactAvailabilityTime() {

        DriverAllocationEngine engine =
                new DriverAllocationEngine(1);

        assertEquals(1, engine.allocate(5, 10));
        assertEquals(1, engine.allocate(15, 5));
    }

    @Test
    void shouldRejectWhenAllDriversBusy() {

        DriverAllocationEngine engine =
                new DriverAllocationEngine(2);

        engine.allocate(1, 10);
        engine.allocate(2, 10);

        assertEquals(-1, engine.allocate(3, 5));
    }

    @Test
    void shouldAlwaysChooseLowestAvailableDriver() {

        DriverAllocationEngine engine =
                new DriverAllocationEngine(3);

        engine.allocate(1, 5);
        engine.allocate(1, 10);

        assertEquals(3, engine.allocate(2, 5));
        assertEquals(1, engine.allocate(6, 5));
    }

    @Test
    void shouldRejectInvalidInputs() {

        assertThrows(IllegalArgumentException.class,
                () -> new DriverAllocationEngine(0));

        DriverAllocationEngine engine =
                new DriverAllocationEngine(2);

        assertThrows(IllegalArgumentException.class,
                () -> engine.allocate(-1, 10));

        assertThrows(IllegalArgumentException.class,
                () -> engine.allocate(1, 0));
    }

    @Test
    void shouldPreventAvailabilityOverflow() {

        DriverAllocationEngine engine =
                new DriverAllocationEngine(1);

        assertThrows(ArithmeticException.class,
                () -> engine.allocate(Long.MAX_VALUE, 1));
    }

    @Test
    void shouldProtectInternalAvailabilityState() {

        DriverAllocationEngine engine =
                new DriverAllocationEngine(1);

        engine.allocate(1, 10);

        long[] snapshot = engine.availabilitySnapshot();
        snapshot[0] = 0;

        assertArrayEquals(
                new long[]{11},
                engine.availabilitySnapshot());
    }
}

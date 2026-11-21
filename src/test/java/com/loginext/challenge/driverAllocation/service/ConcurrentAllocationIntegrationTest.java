
package com.loginext.challenge.driverAllocation.service;

import com.loginext.challenge.driverAllocation.dto.CreateOrderRequest;
import com.loginext.challenge.driverAllocation.dto.OrderAllocationResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrentAllocationIntegrationTest {

    @Autowired
    private PersistentAllocationService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPreventDoubleBooking() throws Exception {

        // Safety: never modify the development database.
        try (Connection connection =
                     jdbcTemplate.getDataSource().getConnection()) {

            assertEquals(
                    "driver_allocation_test",
                    connection.getCatalog()
            );
        }

        // Reset the isolated test database.
        jdbcTemplate.execute("""
            TRUNCATE TABLE
                driver_allocations, orders, drivers
            RESTART IDENTITY CASCADE
            """);

        jdbcTemplate.update("""
            UPDATE allocation_coordination
            SET last_order_time = -1,
                updated_at = NOW()
            WHERE id = 1
            """);

        // Exactly one available driver.
        jdbcTemplate.update("""
            INSERT INTO drivers
                (driver_code, status, available_at)
            VALUES ('DRIVER-1', 'ACTIVE', 0)
            """);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<OrderAllocationResponse> taskA = () -> {
                ready.countDown();
                start.await();

                return service.allocate(
                        new CreateOrderRequest(
                                "ORDER-A", 10L, 20L
                        )
                );
            };

            Callable<OrderAllocationResponse> taskB = () -> {
                ready.countDown();
                start.await();

                return service.allocate(
                        new CreateOrderRequest(
                                "ORDER-B", 10L, 15L
                        )
                );
            };

            Future<OrderAllocationResponse> futureA =
                    executor.submit(taskA);

            Future<OrderAllocationResponse> futureB =
                    executor.submit(taskB);

            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "Workers did not become ready"
            );

            start.countDown();

            OrderAllocationResponse responseA =
                    futureA.get(10, TimeUnit.SECONDS);

            OrderAllocationResponse responseB =
                    futureB.get(10, TimeUnit.SECONDS);

            List<OrderAllocationResponse> results =
                    List.of(responseA, responseB);

            long assigned = results.stream()
                    .filter(r -> "ASSIGNED".equals(r.status()))
                    .count();

            long rejected = results.stream()
                    .filter(r -> "REJECTED".equals(r.status()))
                    .count();

            assertEquals(1, assigned);
            assertEquals(1, rejected);

            Long allocationCount =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM driver_allocations
                            """,
                            Long.class
                    );

            assertEquals(1L, allocationCount);

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}


package com.loginext.challenge.driverAllocation.service;

import com.loginext.challenge.driverAllocation.domain.entity.DriverEntity;
import com.loginext.challenge.driverAllocation.dto.CreateOrderRequest;
import com.loginext.challenge.driverAllocation.dto.OrderAllocationResponse;
import com.loginext.challenge.driverAllocation.exception.DuplicateOrderConflictException;
import com.loginext.challenge.driverAllocation.exception.InvalidOrderSequenceException;
import com.loginext.challenge.driverAllocation.repository.DriverRepository;

import com.loginext.challenge.driverAllocation.inbox.AllocationEventProjection;
import com.loginext.challenge.driverAllocation.kafka.events.AllocationEventCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PersistentAllocationServiceIntegrationTest {
    @Autowired
    private PersistentAllocationService service;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired private AllocationEventCodec codec;
    @Autowired private AllocationEventProjection projection;

    private String orderReferencePrefix;

    @BeforeEach
    void setUp() throws SQLException {

        orderReferencePrefix = UUID.randomUUID().toString();

        // Verify that tests use the dedicated test database.
        try (Connection connection =
                     jdbcTemplate.getDataSource().getConnection()) {

            String databaseName = connection.getCatalog();

            assertEquals(
                    "driver_allocation_test",
                    databaseName,
                    "Integration tests must use the test database"
            );
        }

        // Reset test data before every test.
        // NEVER run this against a production database.
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    driver_allocations,
                    orders,
                    drivers
                RESTART IDENTITY CASCADE
                """);

        // Reset the logical processing clock.
        jdbcTemplate.update("""
                UPDATE allocation_coordination
                SET last_order_time = -1,
                    updated_at = NOW()
                WHERE id = 1
                """);

        // Verify that the coordination row exists.
        Long lastOrderTime = jdbcTemplate.queryForObject(
                """
                SELECT last_order_time
                FROM allocation_coordination
                WHERE id = 1
                """,
                Long.class
        );

        assertNotNull(lastOrderTime);
        assertEquals(-1L, lastOrderTime);


    }

    @Test
    void shouldReturnSameAllocationForDuplicateOrder() {

        createDriver();

        CreateOrderRequest request = new CreateOrderRequest(
                orderReferencePrefix + "-1",
                10L,
                20L
        );

        OrderAllocationResponse first = service.allocate(request);
        OrderAllocationResponse second = service.allocate(request);

        assertEquals(first, second);

        assertEquals("ASSIGNED", first.status());
        assertNotNull(first.driverId());
        assertEquals(30L, first.availableAt());

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders",
                Integer.class
        );

        Integer allocationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM driver_allocations",
                Integer.class
        );

        assertEquals(1, orderCount);
        assertEquals(1, allocationCount);
    }

    @Test
    void shouldRejectConflictingDuplicate() {

        createDriver();

        String orderReference = orderReferencePrefix + "-2";

        CreateOrderRequest firstRequest = new CreateOrderRequest(
                orderReference,
                10L,
                20L
        );

        service.allocate(firstRequest);

        CreateOrderRequest conflictingRequest = new CreateOrderRequest(
                orderReference,
                10L,
                30L
        );

        assertThrows(
                DuplicateOrderConflictException.class,
                () -> service.allocate(conflictingRequest)
        );

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders",
                Integer.class
        );

        Integer allocationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM driver_allocations",
                Integer.class
        );

        assertEquals(1, orderCount);
        assertEquals(1, allocationCount);
    }

    @Test
    void shouldRejectEarlierOrderTime() {

        createDriver();

        CreateOrderRequest firstRequest = new CreateOrderRequest(
                orderReferencePrefix + "-3",
                20L,
                10L
        );

        service.allocate(firstRequest);

        CreateOrderRequest earlierRequest = new CreateOrderRequest(
                orderReferencePrefix + "-4",
                10L,
                10L
        );

        assertThrows(
                InvalidOrderSequenceException.class,
                () -> service.allocate(earlierRequest)
        );

        Long lastOrderTime = jdbcTemplate.queryForObject(
                """
                SELECT last_order_time
                FROM allocation_coordination
                WHERE id = 1
                """,
                Long.class
        );

        assertEquals(20L, lastOrderTime);

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders",
                Integer.class
        );

        assertEquals(1, orderCount);
    }

    @Test
    void shouldRecordOneOutboxEventAndApplyEachConsumerOnce() {
        createDriver();
        var request = new CreateOrderRequest(orderReferencePrefix + "-event", 10L, 20L);
        assertEquals(service.allocate(request), service.allocate(request));
        assertEquals(1L, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM allocation_outbox WHERE order_reference = ?",
                Long.class, request.orderReference()));
        String payload = jdbcTemplate.queryForObject(
                "SELECT payload FROM allocation_outbox WHERE order_reference = ?",
                String.class, request.orderReference());
        var event = codec.decode(request.orderReference(), payload);
        assertEquals("DRIVER_ALLOCATED", event.eventType());
        assertTrue(projection.process("test-audit", event));
        assertFalse(projection.process("test-audit", event));
        assertTrue(projection.process("test-notification", event));
        assertEquals(2L, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM allocation_event_projection WHERE order_reference = ?",
                Long.class, request.orderReference()));
    }

    @Test
    void shouldRecordRejectedOutcome() {
        var request = new CreateOrderRequest(orderReferencePrefix + "-rejected", 10L, 20L);
        assertEquals("REJECTED", service.allocate(request).status());
        String payload = jdbcTemplate.queryForObject(
                "SELECT payload FROM allocation_outbox WHERE order_reference = ?",
                String.class, request.orderReference());
        var event = codec.decode(request.orderReference(), payload);
        assertEquals("ALLOCATION_REJECTED", event.eventType());
        assertNull(event.driverId());
    }

    @Test
    void shouldRollbackInboxWhenProjectionFails() {
        var reference = orderReferencePrefix + "-conflict";
        var first = com.loginext.challenge.driverAllocation.kafka.events.AllocationOutcomeEvent.create(reference, 10, 20, 1L, 30L);
        var second = com.loginext.challenge.driverAllocation.kafka.events.AllocationOutcomeEvent.create(reference, 10, 20, 1L, 30L);
        assertTrue(projection.process("test-audit", first));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> projection.process("test-audit", second));
        assertEquals(0L, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM allocation_inbox WHERE consumer_name = ? AND event_id = ?",
                Long.class, "test-audit", second.eventId()));
    }

    private DriverEntity createDriver() {

        return transactionTemplate.execute(status -> {

            String driverCode = "TEST-" + UUID.randomUUID();

            return driverRepository.saveAndFlush(
                    new DriverEntity(driverCode)
            );
        });
    }
}

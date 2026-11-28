package com.loginext.challenge.driverAllocation.inbox;

import com.loginext.challenge.driverAllocation.kafka.events.AllocationOutcomeEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AllocationEventProjection {

    private final JdbcTemplate jdbc;

    public AllocationEventProjection(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean process(String consumer, AllocationOutcomeEvent event) {
        int claimed = jdbc.update("""
            INSERT INTO allocation_inbox (consumer_name, event_id)
            VALUES (?, ?) ON CONFLICT (consumer_name, event_id) DO NOTHING
            """, consumer, event.eventId());
        if (claimed == 0) return false;

        jdbc.update("""
            INSERT INTO allocation_event_projection
                (consumer_name, order_reference, event_id, event_type, driver_id, available_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """, consumer, event.orderReference(), event.eventId(), event.eventType(),
                event.driverId(), event.availableAt());
        return true;
    }
}

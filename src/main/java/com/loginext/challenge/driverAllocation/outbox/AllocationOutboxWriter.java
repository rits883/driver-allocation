package com.loginext.challenge.driverAllocation.outbox;

import com.loginext.challenge.driverAllocation.kafka.events.AllocationEventCodec;
import com.loginext.challenge.driverAllocation.kafka.events.AllocationOutcomeEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AllocationOutboxWriter {

    private final JdbcTemplate jdbc;
    private final AllocationEventCodec codec;

    public AllocationOutboxWriter(JdbcTemplate jdbc, AllocationEventCodec codec) {
        this.jdbc = jdbc;
        this.codec = codec;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AllocationOutcomeEvent event) {
        jdbc.update("""
            INSERT INTO allocation_outbox (event_id, order_reference, payload)
            VALUES (?, ?, ?)
            """, event.eventId(), event.orderReference(), codec.encode(event));
    }
}

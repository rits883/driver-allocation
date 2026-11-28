package com.loginext.challenge.driverAllocation.outbox;

import com.loginext.challenge.driverAllocation.kafka.config.AllocationKafkaConfig;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(prefix = "allocation.kafka", name = "enabled", havingValue = "true")
public class AllocationOutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(AllocationOutboxRelay.class);
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<Object, Object> kafka;
    private final TransactionTemplate transaction;

    public AllocationOutboxRelay(JdbcTemplate jdbc, KafkaTemplate<Object, Object> kafka,
            TransactionTemplate transaction) {
        this.jdbc = jdbc;
        this.kafka = kafka;
        this.transaction = transaction;
    }

    private record Pending(UUID id, String key, String payload) {}

    @Scheduled(fixedDelayString = "${allocation.kafka.poll-ms:1000}")
    public void publishNext() {
        try {
            transaction.executeWithoutResult(status -> {
                var rows = jdbc.query("""
                    SELECT event_id, order_reference, payload
                    FROM allocation_outbox
                    WHERE published_at IS NULL
                    ORDER BY created_at, event_id
                    LIMIT 1 FOR UPDATE SKIP LOCKED
                    """, (rs, n) -> new Pending(rs.getObject("event_id", UUID.class),
                        rs.getString("order_reference"), rs.getString("payload")));

                if (rows.isEmpty()) return;
                Pending event = rows.get(0);
                try {
                    kafka.send(AllocationKafkaConfig.TOPIC, event.key(), event.payload())
                            .get(15, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Outbox relay interrupted", ex);
                } catch (Exception ex) {
                    throw new IllegalStateException("Kafka publication failed", ex);
                }
                jdbc.update("UPDATE allocation_outbox SET published_at = NOW() WHERE event_id = ?",
                        event.id());
                log.info("Kafka acknowledged outbox eventId={}", event.id());
            });
        } catch (RuntimeException ex) {
            log.warn("Outbox transaction failed; pending event remains eligible", ex);
        }
    }
}

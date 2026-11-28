package com.loginext.challenge.driverAllocation.kafka.consumers;

import com.loginext.challenge.driverAllocation.inbox.AllocationEventProjection;
import com.loginext.challenge.driverAllocation.kafka.config.AllocationKafkaConfig;
import com.loginext.challenge.driverAllocation.kafka.events.AllocationEventCodec;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "allocation.kafka", name = "enabled", havingValue = "true")
public class AllocationAuditConsumer {
    private static final Logger log = LoggerFactory.getLogger(AllocationAuditConsumer.class);
    private final AllocationEventCodec codec;
    private final AllocationEventProjection projection;

    public AllocationAuditConsumer(AllocationEventCodec codec, AllocationEventProjection projection) {
        this.codec = codec;
        this.projection = projection;
    }

    @KafkaListener(topics = AllocationKafkaConfig.TOPIC, groupId = AllocationKafkaConfig.AUDIT_GROUP,
            containerFactory = "allocationAuditFactory")
    public void consume(ConsumerRecord<String, String> record) {
        var event = codec.decode(record.key(), record.value());
        boolean applied = projection.process(AllocationKafkaConfig.AUDIT_GROUP, event);
        log.info("Audit eventId={} applied={} partition={} offset={}",
                event.eventId(), applied, record.partition(), record.offset());
    }
}

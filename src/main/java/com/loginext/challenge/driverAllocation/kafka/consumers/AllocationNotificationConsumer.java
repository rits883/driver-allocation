package com.loginext.challenge.driverAllocation.kafka.consumers;

import com.loginext.challenge.driverAllocation.inbox.AllocationEventProjection;
import com.loginext.challenge.driverAllocation.kafka.config.AllocationKafkaConfig;
import com.loginext.challenge.driverAllocation.kafka.events.AllocationEventCodec;
import com.loginext.challenge.driverAllocation.kafka.events.InvalidAllocationEventException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "allocation.kafka", name = "enabled", havingValue = "true")
public class AllocationNotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(AllocationNotificationConsumer.class);
    private final AllocationEventCodec codec;
    private final AllocationEventProjection projection;

    public AllocationNotificationConsumer(AllocationEventCodec codec, AllocationEventProjection projection) {
        this.codec = codec;
        this.projection = projection;
    }

    @RetryableTopic(attempts = "4",
            backOff = @BackOff(delay = 5000, multiplier = 2.0, maxDelay = 20000),
            retryTopicSuffix = "-notification-retry", dltTopicSuffix = "-notification-dlt",
            numPartitions = "${allocation.kafka.partitions:3}",
            replicationFactor = "${allocation.kafka.replication-factor:1}",
            exclude = InvalidAllocationEventException.class, traversingCauses = "true")
    @KafkaListener(topics = AllocationKafkaConfig.TOPIC,
            groupId = AllocationKafkaConfig.NOTIFICATION_GROUP)
    public void consume(ConsumerRecord<String, String> record) {
        var event = codec.decode(record.key(), record.value());
        // Persist a notification intent, not an actual SMS/email side effect.
        boolean applied = projection.process(AllocationKafkaConfig.NOTIFICATION_GROUP, event);
        log.info("Notification intent eventId={} applied={} topic={} partition={} offset={}",
                event.eventId(), applied, record.topic(), record.partition(), record.offset());
    }

    @DltHandler
    public void onDlt(ConsumerRecord<String, String> record) {
        log.error("Notification DLT topic={} partition={} offset={} key={}",
                record.topic(), record.partition(), record.offset(), record.key());
    }
}

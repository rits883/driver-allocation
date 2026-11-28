package com.loginext.challenge.driverAllocation.kafka.config;

import com.loginext.challenge.driverAllocation.kafka.events.InvalidAllocationEventException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "allocation.kafka", name = "enabled", havingValue = "true")
public class AllocationKafkaConfig {

    public static final String TOPIC = "driver-allocation-events";
    public static final String AUDIT_DLT = TOPIC + "-audit-dlt";
    public static final String AUDIT_GROUP = "allocation-audit";
    public static final String NOTIFICATION_GROUP = "allocation-notification";

    @Bean
    NewTopic allocationEventsTopic(@Value("${allocation.kafka.partitions:3}") int partitions,
            @Value("${allocation.kafka.replication-factor:1}") int replicas) {
        return TopicBuilder.name(TOPIC).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    NewTopic allocationAuditDlt(@Value("${allocation.kafka.partitions:3}") int partitions,
            @Value("${allocation.kafka.replication-factor:1}") int replicas) {
        return TopicBuilder.name(AUDIT_DLT).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Object, Object> allocationAuditFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(AUDIT_DLT, record.partition()));
        recoverer.setFailIfSendResultIsError(true);
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 2L));
        handler.addNotRetryableExceptions(InvalidAllocationEventException.class);
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(handler);
        return factory;
    }
}

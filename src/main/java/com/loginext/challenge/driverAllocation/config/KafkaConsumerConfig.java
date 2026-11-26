package com.loginext.challenge.driverAllocation.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaConsumerConfig.class);

    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate)
    {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate,(record,exception)->
                        new TopicPartition(record.topic()+".DTL", record.partition() ));
        recoverer.setFailIfSendResultIsError(true);

        FixedBackOff backOff = new FixedBackOff(2000L,2L);

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer,backOff);

        errorHandler.setRetryListeners(
                (record, exception, deliveryAttempt) ->
                        log.warn(
                                "Processing failed: topic={}, partition={}, " +
                                        "offset={}, attempt={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                deliveryAttempt
                        )
        );

        return errorHandler;
    };

}

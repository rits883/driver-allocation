package com.loginext.challenge.driverAllocation.kafka.consumers;

import com.loginext.challenge.driverAllocation.service.DriverDeliveryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("kafka-lab")
public class DriverDeliveryConsumer {


    private static final Logger log =
            LoggerFactory.getLogger(
                    DriverDeliveryConsumer.class);

    private final DriverDeliveryService service;


    public DriverDeliveryConsumer(
            DriverDeliveryService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = "driver-delivery-idempotency-lab",
            groupId = "driver-delivery-service"
    )
    public void consume( ConsumerRecord<String, String> record) {

        log.info(
                "Received key={}, value={}, partition={}, offset={}",
                record.key(),
                record.value(),
                record.partition(),
                record.offset()
        );

        String driverId = record.key();

        String eventId = record.value();

        service.process(eventId, driverId);
    }









}

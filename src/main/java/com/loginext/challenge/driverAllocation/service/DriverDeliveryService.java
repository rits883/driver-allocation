package com.loginext.challenge.driverAllocation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverDeliveryService {
    private static final Logger log =
            LoggerFactory.getLogger(DriverDeliveryService.class);

    private static final String CONSUMER_NAME =
            "driver-delivery-service";

    private final JdbcTemplate jdbcTemplate;

    public DriverDeliveryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Transactional
    public void process(String eventId, String driverId){


        int inserted = jdbcTemplate.update("""
            INSERT INTO lab_processed_events (
                consumer_name,
                event_id
            )
            VALUES (?, ?)
            ON CONFLICT (consumer_name, event_id)
            DO NOTHING
            """,
                CONSUMER_NAME,
                eventId
        );

        if (inserted == 0) {

            log.info(
                    "Duplicate event ignored: eventId={}",
                    eventId
            );

            return;
        }

        if ("EVT-FAIL-004".equals(eventId)) {

            log.warn(
                    "Simulating processing failure for eventId={}",
                    eventId
            );

            throw new IllegalStateException(
                    "Simulated driver delivery processing failure"
            );
        }

        int updated = jdbcTemplate.update("""
            UPDATE lab_driver_delivery_stats
            SET completed_deliveries =
                completed_deliveries + 1
            WHERE driver_id = ?
            """,
                driverId
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Driver not found: " + driverId
            );
        }

        log.info(
                "Event processed: eventId={}, driverId={}",
                eventId,
                driverId
        );






    }





}

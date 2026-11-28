package com.loginext.challenge.driverAllocation.kafka.events;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AllocationEventCodec {

    private final JsonMapper mapper;

    public AllocationEventCodec(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public String encode(AllocationOutcomeEvent event) {
        return mapper.writeValueAsString(event);
    }

    public AllocationOutcomeEvent decode(String key, String payload) {
        try {
            AllocationOutcomeEvent event = mapper.readValue(payload, AllocationOutcomeEvent.class);
            if (event == null || !event.orderReference().equals(key)) {
                throw new IllegalArgumentException("Kafka key must match order reference");
            }
            return event;
        } catch (RuntimeException ex) {
            throw new InvalidAllocationEventException("Invalid allocation event", ex);
        }
    }
}

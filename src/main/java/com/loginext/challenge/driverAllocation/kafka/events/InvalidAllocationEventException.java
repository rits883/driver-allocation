package com.loginext.challenge.driverAllocation.kafka.events;

public class InvalidAllocationEventException extends RuntimeException {
    public InvalidAllocationEventException(String message, Throwable cause) {
        super(message, cause);
    }
}

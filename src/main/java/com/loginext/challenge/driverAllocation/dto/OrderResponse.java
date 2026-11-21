package com.loginext.challenge.driverAllocation.dto;

public record OrderResponse(  Long id,
                              String orderReference,
                              Long orderTime,
                              Long travelTime,
                              String status,
                              Long driverId) {
}

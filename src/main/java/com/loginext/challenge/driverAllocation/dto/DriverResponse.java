package com.loginext.challenge.driverAllocation.dto;

public record DriverResponse(    Long id,
                                 String driverCode,
                                 String status,
                                 Long availableAt) {
}

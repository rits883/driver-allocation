package com.loginext.challenge.driverAllocation.dto;

public record OrderAllocationResponse(  String orderReference,
                                        String status,
                                        Long driverId,
                                        Long availableAt) {
}

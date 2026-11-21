package com.loginext.challenge.driverAllocation.dto;

import com.loginext.challenge.driverAllocation.domain.enums.DriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DriverStatusRequest(

        @Schema(
                description = "Requested driver status",
                example = "INACTIVE"
        )
        @NotNull DriverStatus status) {
}

package com.loginext.challenge.driverAllocation.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateDriverRequest(

        @Schema(
                description = "Unique business identifier for a driver",
                example = "DRIVER-101"
        )
        @NotBlank
                                   @Size(max = 50)
                                   String driverCode) {
}

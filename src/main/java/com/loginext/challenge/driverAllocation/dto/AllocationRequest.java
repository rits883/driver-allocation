package com.loginext.challenge.driverAllocation.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;


public record AllocationRequest(
        @NotNull
        @Positive
        @Max(10_000)
        Integer driverCount,

        @NotEmpty
        @Size(max = 100_000)
        List<@NotNull @Valid OrderRequest> orders) {
}

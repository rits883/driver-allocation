package com.loginext.challenge.driverAllocation.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderRequest(

    @NotNull
    @PositiveOrZero
    Long orderTime ,

    @NotNull
    @Positive
    Long travelTime
)
{}

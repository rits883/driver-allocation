package com.loginext.challenge.driverAllocation.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateOrderRequest(@NotBlank String orderReference,
                                 @NotNull @PositiveOrZero Long orderTime,
                                 @NotNull @Positive Long travelTime) {
}

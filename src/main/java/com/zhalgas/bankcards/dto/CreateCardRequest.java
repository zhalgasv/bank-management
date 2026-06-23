package com.zhalgas.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CreateCardRequest(
        @NotNull Long ownerId,

        @NotNull
        @Future
        YearMonth expiryDate,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal initialBalance
) {
}

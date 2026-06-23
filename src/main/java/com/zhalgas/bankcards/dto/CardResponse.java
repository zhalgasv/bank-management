package com.zhalgas.bankcards.dto;

import com.zhalgas.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

public record CardResponse(
        Long id,
        String maskedNumber,
        Long ownerId,
        String ownerUsername,
        YearMonth expiryDate,
        CardStatus status,
        BigDecimal balance,
        Instant createdAt
) {
}

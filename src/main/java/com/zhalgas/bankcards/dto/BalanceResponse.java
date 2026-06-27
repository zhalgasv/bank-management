package com.zhalgas.bankcards.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        Long cardId,
        BigDecimal balance
) {
}

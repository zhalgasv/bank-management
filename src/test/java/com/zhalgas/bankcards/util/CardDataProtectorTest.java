package com.zhalgas.bankcards.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardDataProtectorTest {

    private final CardDataProtector protector =
            new CardDataProtector("MDEyMzQ1Njc4OWFiY2RlZg==");

    @Test
    void maskReturnsMaskedCardNumber() {
        String result = protector.mask("1234");

        assertThat(result).isEqualTo("**** **** **** 1234");
    }

    @Test
    void maskThrowsExceptionWhenLastFourInvalid() {
        assertThatThrownBy(() -> protector.mask("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lastFour must contain exactly 4 characters");
    }

    @Test
    void hashReturnsSha256Hex() {
        String result = protector.hash("1234567812345678");

        assertThat(result).hasSize(64);
        assertThat(result).matches("[0-9a-f]{64}");
    }

    @Test
    void hashThrowsExceptionWhenCardNumberInvalid() {
        assertThatThrownBy(() -> protector.hash("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card number must contain exactly 16 digits");
    }

    @Test
    void encryptReturnsDifferentValuesForSameCardNumber() {
        String first = protector.encrypt("1234567812345678");
        String second = protector.encrypt("1234567812345678");

        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void encryptThrowsExceptionWhenCardNumberInvalid() {
        assertThatThrownBy(() -> protector.encrypt("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card number must contain exactly 16 digits");
    }
}

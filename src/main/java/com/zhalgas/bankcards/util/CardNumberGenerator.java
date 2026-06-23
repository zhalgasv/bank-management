package com.zhalgas.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder number = new StringBuilder();

        while (number.length() < 16) {
            number.append(secureRandom.nextInt(10));
        }
        return number.toString();
    }
}

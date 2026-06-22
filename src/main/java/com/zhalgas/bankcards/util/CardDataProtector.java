package com.zhalgas.bankcards.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

@Component
public class CardDataProtector {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public String mask(String lastFour) {
        if( lastFour == null || lastFour.length() != 4) {
            throw new IllegalArgumentException(
                    "lastFour must contain exactly 4 characters"
            );
        }
        return "**** **** **** " + lastFour;
    }

    public String hash(String cardNumber) {

        if(cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException(
                    "Card number must contain exactly 16 digits"
            );
        }
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    cardNumber.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }

    public CardDataProtector(
            @Value("${app.card-encryption.key}") String base64Key
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);

        if(keyBytes.length != 16
                && keyBytes.length != 24
                && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Encryption key must contain 16, 24, or 32 bytes"
            );
        }
        this.encryptionKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException(
                    "Card number must contain exactly 16 digits"
            );
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    encryptionKey,
                    new GCMParameterSpec(TAG_LENGTH, iv)
            );

            byte[] encrypted = cipher.doFinal(
                    cardNumber.getBytes(StandardCharsets.UTF_8)
            );

            byte[] payload = new byte[iv.length + encrypted.length];

            System.arraycopy(
                    iv,
                    0,
                    payload,
                    0,
                    iv.length
            );

            System.arraycopy(
                    encrypted,
                    0,
                    payload,
                    iv.length,
                    encrypted.length
            );

            return Base64.getEncoder().encodeToString(payload);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not encrypt card number",
                    exception
            );
        }
    }
}

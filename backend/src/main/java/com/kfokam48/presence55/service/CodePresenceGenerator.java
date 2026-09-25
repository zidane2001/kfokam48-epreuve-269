package com.kfokam48.presence55.service;

import java.security.SecureRandom;

/** Code de presence : 6 caracteres A-Z / 2-9, sans caracteres ambigus (0/O, 1/I/L). */
public final class CodePresenceGenerator {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RNG = new SecureRandom();

    private CodePresenceGenerator() { }

    public static String generer() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHABET.charAt(RNG.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

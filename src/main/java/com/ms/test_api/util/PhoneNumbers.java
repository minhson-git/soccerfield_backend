package com.ms.test_api.util;

import java.util.regex.Pattern;

public final class PhoneNumbers {

    private static final Pattern NON_DIGITS = Pattern.compile("[^0-9]");

    private PhoneNumbers() {
    }

    public static String normalizeVietnamese(String raw) {
        if (raw == null || raw.isBlank())
            return null;

        String digits = NON_DIGITS.matcher(raw.trim()).replaceAll("");

        if (digits.startsWith("84")) {
            digits = digits.substring(2);
        } else if (digits.startsWith("0")) {
            digits = digits.substring(1);
        } else {
            return null;
        }

        if (!digits.matches("[35789]\\d{8}")) {
            return null;
        }

        return "+84" + digits;
    }

    public static boolean looksLikePhoneNumber(String raw) {
        return normalizeVietnamese(raw) != null;
    }
}
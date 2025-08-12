package com.bina.cloud.util;

import java.util.regex.Pattern;

public class PhoneNumberUtil {
    private static final Pattern PHONE_JSON_PATTERN = Pattern.compile(".*\"numero\":\\s*\"([^\"]+)\".*");
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("[^0-9]");

    public static String extractPhoneNumber(String additionalData) {
        if (additionalData == null || additionalData.isEmpty()) {
            return "";
        }

        try {
            // Tentar extrair número usando regex para JSON
            String phoneNumber = PHONE_JSON_PATTERN.matcher(additionalData).replaceAll("$1");

            // Se não encontrou no formato JSON, tentar outros padrões
            if (phoneNumber.equals(additionalData)) {
                // Tentar extrair apenas números
                phoneNumber = DIGITS_ONLY_PATTERN.matcher(additionalData).replaceAll("");
            }

            if (!phoneNumber.isEmpty() && !"N/A".equals(phoneNumber)) {
                // Remover zeros à esquerda (se o número começar com 0, ignorar o 0)
                while (phoneNumber.startsWith("0")) {
                    phoneNumber = phoneNumber.substring(1);
                }

                // Normalizar para 11 dígitos (DDD + número)
                // Só truncar se tiver mais de 11 dígitos
                if (phoneNumber.length() > 11) {
                    phoneNumber = phoneNumber.substring(0, 11);
                }
                return phoneNumber;
            }

        } catch (Exception e) {
            // Log error silently
        }

        return "";
    }

}

package com.bina.cloud.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimezoneUtil {

    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    public static final ZoneId BRASILIA_ZONE = ZoneId.of("America/Sao_Paulo");
    public static final DateTimeFormatter BRASILIA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Converte LocalDateTime de UTC para Brasília
     */
    public static String convertUtcToBrasilia(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return "N/A";
        }

        ZonedDateTime utcZoned = utcDateTime.atZone(UTC_ZONE);
        ZonedDateTime brasiliaZoned = utcZoned.withZoneSameInstant(BRASILIA_ZONE);
        return brasiliaZoned.format(BRASILIA_FORMATTER);
    }

    /**
     * Converte LocalDateTime de UTC para Brasília retornando LocalDateTime
     */
    public static LocalDateTime convertUtcToBrasiliaDateTime(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }

        ZonedDateTime utcZoned = utcDateTime.atZone(UTC_ZONE);
        ZonedDateTime brasiliaZoned = utcZoned.withZoneSameInstant(BRASILIA_ZONE);
        return brasiliaZoned.toLocalDateTime();
    }

    /**
     * Obtém o timestamp atual em UTC
     */
    public static LocalDateTime getCurrentUtcTime() {
        return LocalDateTime.now(UTC_ZONE);
    }

    /**
     * Obtém o timestamp atual em Brasília
     */
    public static LocalDateTime getCurrentBrasiliaTime() {
        return LocalDateTime.now(BRASILIA_ZONE);
    }

    /**
     * Converte string de timestamp para Brasília (aceita tanto ISO quanto formato
     * brasileiro)
     */
    public static String convertTimestampToBrasilia(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            return "N/A";
        }

        try {
            // Verificar se já está no formato brasileiro
            if (timestamp.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
                return timestamp;
            }

            // Tentar converter de ISO
            LocalDateTime utcTime = LocalDateTime.parse(timestamp, ISO_FORMATTER);
            return convertUtcToBrasilia(utcTime);

        } catch (Exception e) {
            // Se não conseguir converter, retornar original
            return timestamp;
        }
    }
}
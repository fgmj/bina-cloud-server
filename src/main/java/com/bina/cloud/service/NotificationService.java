package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final EventoRepository eventoRepository;
    private final ZoneId brasiliaZone = ZoneId.of("America/Sao_Paulo");
    private final ZoneId utcZone = ZoneId.of("UTC");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void notifyNewEvent(String eventId, String eventTitle, String eventType, String deviceId, String timestamp,
            String additionalData) {
        log.info("Processando novo evento: {} - {}", eventType, eventTitle);

        // Converter timestamp para Brasília
        String brasiliaTimestamp = convertToBrasiliaTime(timestamp);

        // Extrair o número de telefone do additionalData
        String phoneNumber = extractPhoneNumber(additionalData);
        String url = "";
        String timeSinceLastCall = "";

        if (!phoneNumber.isEmpty()) {
            url = String.format("https://portal.gasdelivery.com.br/secure/client/?primary_phone=%s", phoneNumber);
            log.info("URL do Gas Delivery gerada: {}", url);

            // Calcular tempo desde a última ligação
            timeSinceLastCall = calculateTimeSinceLastCall(phoneNumber);
        } else {
            log.warn("Nenhum número de telefone encontrado em additionalData: {}", additionalData);
        }

        EventNotification notification = new EventNotification(
                eventId, eventTitle, eventType, deviceId, brasiliaTimestamp, additionalData, url, timeSinceLastCall,
                phoneNumber);

        messagingTemplate.convertAndSend("/topic/events", notification);
        log.info("Notificação enviada via WebSocket para evento ID: {}", eventId);
    }

    private String convertToBrasiliaTime(String timestamp) {
        try {
            // Verificar se o timestamp já está no formato brasileiro
            if (timestamp.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
                // Já está no formato correto, retornar como está
                return timestamp;
            }

            // Se não estiver no formato brasileiro, tentar converter de ISO
            LocalDateTime utcTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            ZonedDateTime utcZoned = utcTime.atZone(utcZone);
            ZonedDateTime brasiliaZoned = utcZoned.withZoneSameInstant(brasiliaZone);

            return brasiliaZoned.format(formatter);
        } catch (Exception e) {
            log.error("Erro ao converter timestamp para Brasília: {}", timestamp, e);
            return timestamp; // Retornar original se não conseguir converter
        }
    }

    private String extractPhoneNumber(String additionalData) {
        if (additionalData == null || additionalData.isEmpty()) {
            return "";
        }

        try {
            // Tentar extrair número usando regex para JSON
            String phoneNumber = additionalData.replaceAll(".*\"numero\":\\s*\"([^\"]+)\".*", "$1");

            // Se não encontrou no formato JSON, tentar outros padrões
            if (phoneNumber.equals(additionalData)) {
                // Tentar extrair apenas números
                phoneNumber = additionalData.replaceAll("[^0-9]", "");
            }

            if (!phoneNumber.isEmpty() && !"N/A".equals(phoneNumber)) {
                // Normalizar para 11 dígitos (DDD + número)
                if (phoneNumber.length() > 11) {
                    phoneNumber = phoneNumber.substring(phoneNumber.length() - 11);
                }

                log.debug("Número extraído: {}", phoneNumber);
                return phoneNumber;
            }

        } catch (Exception e) {
            log.error("Erro ao extrair número de telefone de: {}", additionalData, e);
        }

        return "";
    }

    private String calculateTimeSinceLastCall(String phoneNumber) {
        try {
            // Buscar eventos anteriores com o mesmo número de telefone
            List<Evento> previousEvents = eventoRepository.findByPhoneNumberOrderByTimestampDesc(phoneNumber);

            if (previousEvents.size() > 1) { // Mais de 1 porque o atual já está incluído
                Evento lastCall = previousEvents.get(1); // Pega o segundo (anterior ao atual)
                LocalDateTime now = LocalDateTime.now(utcZone);
                Duration duration = Duration.between(lastCall.getTimestamp(), now);

                return formatDuration(duration);
            } else {
                return "Primeira ligação";
            }

        } catch (Exception e) {
            log.error("Erro ao calcular tempo desde última ligação para: {}", phoneNumber, e);
            return "Erro ao calcular";
        }
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            return String.format("%d dia%s, %d hora%s", days, days > 1 ? "s" : "", hours, hours > 1 ? "s" : "");
        } else if (hours > 0) {
            return String.format("%d hora%s, %d minuto%s", hours, hours > 1 ? "s" : "", minutes,
                    minutes > 1 ? "s" : "");
        } else if (minutes > 0) {
            return String.format("%d minuto%s", minutes, minutes > 1 ? "s" : "");
        } else {
            return "Menos de 1 minuto";
        }
    }

    public record EventNotification(
            String eventId,
            String eventTitle,
            String eventType,
            String deviceId,
            String timestamp,
            String additionalData,
            String url,
            String timeSinceLastCall,
            String phoneNumber) {
    }
}
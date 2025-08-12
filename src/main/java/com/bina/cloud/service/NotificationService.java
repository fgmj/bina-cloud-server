package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.util.PhoneNumberUtil;
import com.bina.cloud.util.TimezoneUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final EventoRepository eventoRepository;

    // Static compiled regex patterns to avoid repeated compilation

    public void notifyNewEvent(String eventId, String eventTitle, String eventType, String deviceId, String timestamp,
            String additionalData) {
        long startTime = System.currentTimeMillis();
        log.info("[NotificationService] notifyNewEvent - IN eventId={} eventType={} deviceId={} ", eventId, eventType,
                deviceId);

        try {
            // Converter timestamp para Brasília
            String brasiliaTimestamp = TimezoneUtil.convertTimestampToBrasilia(timestamp);

            // Extrair o número de telefone do additionalData
            String phoneNumber = PhoneNumberUtil.extractPhoneNumber(additionalData);
            String url = "";
            String timeSinceLastCall = "";

            if (!phoneNumber.isEmpty()) {
                url = String.format("https://portal.gasdelivery.com.br/secure/client/?primary_phone=%s", phoneNumber);
                log.debug("[NotificationService] URL do Gas Delivery gerada={}", url);

                // Calcular tempo desde a última ligação
                timeSinceLastCall = calculateTimeSinceLastCall(phoneNumber);
            } else {
                log.warn("[NotificationService] Nenhum número de telefone encontrado: additionalData={}",
                        additionalData);
            }

            EventNotification notification = new EventNotification(
                    eventId, eventTitle, eventType, deviceId, brasiliaTimestamp, additionalData, url, timeSinceLastCall,
                    phoneNumber);

            messagingTemplate.convertAndSend("/topic/events", notification);
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("[NotificationService] notifyNewEvent - OUT success durationMs={}ms", durationMs);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[NotificationService] notifyNewEvent - ERROR durationMs={}ms message={}", durationMs,
                    e.getMessage(), e);
            throw e;
        }
    }

    private String calculateTimeSinceLastCall(String phoneNumber) {
        try {
            // Buscar eventos anteriores com o mesmo número de telefone
            List<Evento> previousEvents = eventoRepository.findByPhoneNumberOrderByTimestampDesc(phoneNumber);

            if (previousEvents.size() > 1) { // Mais de 1 porque o atual já está incluído
                Evento lastCall = previousEvents.get(1); // Pega o segundo (anterior ao atual)
                var now = TimezoneUtil.getCurrentUtcTime();
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
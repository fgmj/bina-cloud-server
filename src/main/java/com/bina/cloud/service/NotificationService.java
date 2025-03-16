package com.bina.cloud.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewEvent(String eventId, String eventTitle, String eventType, String deviceId, String timestamp, String additionalData) {
        // Extrair o número de telefone do additionalData
        String phoneNumber = "";
        if (additionalData != null && !additionalData.isEmpty()) {
            try {
                // Assumindo que additionalData é um JSON com o formato {"numero": "123456789"}
                phoneNumber = additionalData.replaceAll(".*\"numero\":\\s*\"([^\"]+)\".*", "$1");
                if (!"N/A".equals(phoneNumber)) {
                    // Se tiver mais de 11 caracteres, mantém apenas os últimos 11
                    if (phoneNumber.length() > 11) {
                        phoneNumber = phoneNumber.substring(phoneNumber.length() - 11);
                    }
                } else {
                    phoneNumber = "";
                }
            } catch (Exception e) {
                // Se houver erro ao extrair o número, usar string vazia
                phoneNumber = "";
            }
        }

        // Construir URL do portal Gas Delivery com o número de telefone
        String url = phoneNumber.isEmpty() ? "" : 
            String.format("https://portal.gasdelivery.com.br/secure/client/?primary_phone=%s", phoneNumber);

        EventNotification notification = new EventNotification(
            eventId, eventTitle, eventType, deviceId, timestamp, additionalData, url);
        messagingTemplate.convertAndSend("/topic/events", notification);
    }

    public record EventNotification(
        String eventId, 
        String eventTitle, 
        String eventType, 
        String deviceId, 
        String timestamp,
        String additionalData,
        String url
    ) {}
} 
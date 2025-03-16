package com.bina.cloud.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewEvent(String eventId, String eventTitle, String eventType, String deviceId, String timestamp, String additionalData) {
        EventNotification notification = new EventNotification(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);
        messagingTemplate.convertAndSend("/topic/events", notification);
    }

    public record EventNotification(
        String eventId, 
        String eventTitle, 
        String eventType, 
        String deviceId, 
        String timestamp,
        String additionalData
    ) {}
} 
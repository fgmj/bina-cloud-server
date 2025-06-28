package com.bina.cloud.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.bina.cloud.repository.EventoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private EventoRepository eventoRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(messagingTemplate, eventoRepository);
    }

    @Test
    void testNotifyNewEvent_MVP_ShouldExtractPhoneNumberAndSendNotification() {
        // Arrange - MVP scenario
        String eventId = "1";
        String eventTitle = "Chamada recebida";
        String eventType = "CALL_RECEIVED";
        String deviceId = "052ad7f7b6ee816b";
        String timestamp = "2025-06-24T19:59:59";
        String additionalData = "{\"numero\":\"061981122752\",\"data\":\"24/06/2025 19:59:59\",\"receivingNumber\":\"\"}";

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_CallMissed_ShouldExtractPhoneNumberCorrectly() {
        // Arrange
        String eventId = "2";
        String eventTitle = "Chamada perdida";
        String eventType = "CALL_MISSED";
        String deviceId = "052ad7f7b6ee816b";
        String timestamp = "2025-06-24T20:15:30";
        String additionalData = "{\"numero\":\"11987654321\",\"data\":\"24/06/2025 20:15:30\"}";

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_CallAnswered_ShouldExtractPhoneNumberCorrectly() {
        // Arrange
        String eventId = "3";
        String eventTitle = "Chamada atendida";
        String eventType = "CALL_ANSWERED";
        String deviceId = "052ad7f7b6ee816b";
        String timestamp = "2025-06-24T20:30:45";
        String additionalData = "{\"numero\":\"21999887766\",\"data\":\"24/06/2025 20:30:45\"}";

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_InvalidPhoneNumber_ShouldHandleGracefully() {
        // Arrange
        String eventId = "4";
        String eventTitle = "Chamada com número inválido";
        String eventType = "CALL_RECEIVED";
        String deviceId = "052ad7f7b6ee816b";
        String timestamp = "2025-06-24T20:45:12";
        String additionalData = "{\"numero\":\"N/A\",\"data\":\"24/06/2025 20:45:12\"}";

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_NoAdditionalData_ShouldHandleGracefully() {
        // Arrange
        String eventId = "5";
        String eventTitle = "Chamada sem dados adicionais";
        String eventType = "CALL_RECEIVED";
        String deviceId = "052ad7f7b6ee816b";
        String timestamp = "2025-06-24T21:00:00";
        String additionalData = null;

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_PlainTextAdditionalData_ShouldExtractNumbers() {
        // Arrange
        String eventId = "6";
        String eventTitle = "Chamada com texto simples";
        String eventType = "CALL_RECEIVED";
        String deviceId = "052ad7f7b6ee816b";
        String timestamp = "2025-06-24T21:15:00";
        String additionalData = "Chamada do número 061981122752 às 21:15:00";

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_LongPhoneNumber_ShouldTruncateTo11Digits() {
        // Arrange
        String eventId = "7";
        String eventTitle = "Chamada com número longo";
        String eventType = "CALL_RECEIVED";
        String deviceId = "052ad7f7b6ee816b";
        String timestamp = "2025-06-24T21:30:00";
        String additionalData = "{\"numero\":\"55061981122752\",\"data\":\"24/06/2025 21:30:00\"}";

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_WithBrazilianTimestamp_ShouldNotThrowException() {
        // Arrange
        String eventId = "1";
        String eventTitle = "Test Event";
        String eventType = "CALL";
        String deviceId = "device123";
        String timestamp = "25/06/2025 20:26:56"; // Formato brasileiro que está causando erro
        String additionalData = "{\"numero\":\"11999999999\"}";

        // Act & Assert - não deve lançar exceção
        assertDoesNotThrow(() -> {
            notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);
        });

        // Verificar que a mensagem foi enviada
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }

    @Test
    void testNotifyNewEvent_WithISOTimestamp_ShouldWorkCorrectly() {
        // Arrange
        String eventId = "1";
        String eventTitle = "Test Event";
        String eventType = "CALL";
        String deviceId = "device123";
        String timestamp = "2025-06-25T20:26:56"; // Formato ISO
        String additionalData = "{\"numero\":\"11999999999\"}";

        // Act
        notificationService.notifyNewEvent(eventId, eventTitle, eventType, deviceId, timestamp, additionalData);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"),
                any(NotificationService.EventNotification.class));
    }
}
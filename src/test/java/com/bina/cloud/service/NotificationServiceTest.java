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
    void testConvertToBrasiliaTime_WithISOFormat_ShouldSucceed() throws Exception {
        // Arrange
        String isoTimestamp = "2025-06-25T20:26:56";

        // Act
        Method method = NotificationService.class.getDeclaredMethod("convertToBrasiliaTime", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(notificationService, isoTimestamp);

        // Assert
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}"));
        assertEquals("25/06/2025 17:26:56", result); // UTC-3 para Brasília
    }

    @Test
    void testConvertToBrasiliaTime_WithBrazilianFormat_ShouldFail() throws Exception {
        // Arrange
        String brazilianTimestamp = "25/06/2025 20:26:56";

        // Act & Assert
        Method method = NotificationService.class.getDeclaredMethod("convertToBrasiliaTime", String.class);
        method.setAccessible(true);

        // Deve retornar o timestamp original devido ao erro de parsing
        String result = (String) method.invoke(notificationService, brazilianTimestamp);
        assertEquals(brazilianTimestamp, result);
    }

    @Test
    void testConvertToBrasiliaTime_WithInvalidFormat_ShouldReturnOriginal() throws Exception {
        // Arrange
        String invalidTimestamp = "invalid-timestamp";

        // Act
        Method method = NotificationService.class.getDeclaredMethod("convertToBrasiliaTime", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(notificationService, invalidTimestamp);

        // Assert
        assertEquals(invalidTimestamp, result);
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
package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.util.TimezoneUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventoService eventoService;

    private LocalDateTime mockUtcTime;
    private LocalDateTime mockBrasiliaTime;

    @BeforeEach
    void setUp() {
        mockUtcTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        mockBrasiliaTime = LocalDateTime.of(2024, 1, 15, 8, 30, 0);
    }

    @Test
    void testCriarEvento_WithLeadingZero_ShouldRemoveLeadingZero() {
        // Arrange
        Evento evento = new Evento();
        evento.setDescription("Test call");
        evento.setEventType("CALL_RECEIVED");
        evento.setDeviceId("test-device");
        evento.setAdditionalData("{\"numero\": \"061996593711\"}");

        Evento savedEvento = new Evento();
        savedEvento.setId(1L);
        savedEvento.setDescription("Test call");
        savedEvento.setEventType("CALL_RECEIVED");
        savedEvento.setDeviceId("test-device");
        savedEvento.setPhoneNumber("61996593711"); // Should have leading zero removed
        savedEvento.setTimestamp(mockUtcTime);
        savedEvento.setAdditionalData("{\"numero\": \"061996593711\"}"); // Keep original additionalData

        when(eventoRepository.save(any(Evento.class))).thenReturn(savedEvento);

        try (MockedStatic<TimezoneUtil> timezoneUtilMock = mockStatic(TimezoneUtil.class)) {
            timezoneUtilMock.when(TimezoneUtil::getCurrentUtcTime).thenReturn(mockUtcTime);
            timezoneUtilMock.when(() -> TimezoneUtil.convertUtcToBrasilia(mockUtcTime))
                    .thenReturn("15/01/2024 08:30:00");

            // Act
            Evento result = eventoService.criarEvento(evento);

            // Assert
            assertNotNull(result);
            assertEquals("61996593711", result.getPhoneNumber()); // Leading zero should be removed
            verify(notificationService).notifyNewEvent(
                    eq("1"),
                    eq("Test call"),
                    eq("CALL_RECEIVED"),
                    eq("test-device"),
                    eq("15/01/2024 08:30:00"),
                    eq("{\"numero\": \"061996593711\"}"));
        }
    }

    @Test
    void testCriarEvento_WithoutLeadingZero_ShouldKeepAsIs() {
        // Arrange
        Evento evento = new Evento();
        evento.setDescription("Test call");
        evento.setEventType("CALL_RECEIVED");
        evento.setDeviceId("test-device");
        evento.setAdditionalData("{\"numero\": \"61996593711\"}");

        Evento savedEvento = new Evento();
        savedEvento.setId(1L);
        savedEvento.setDescription("Test call");
        savedEvento.setEventType("CALL_RECEIVED");
        savedEvento.setDeviceId("test-device");
        savedEvento.setPhoneNumber("61996593711");
        savedEvento.setTimestamp(mockUtcTime);

        when(eventoRepository.save(any(Evento.class))).thenReturn(savedEvento);

        try (MockedStatic<TimezoneUtil> timezoneUtilMock = mockStatic(TimezoneUtil.class)) {
            timezoneUtilMock.when(TimezoneUtil::getCurrentUtcTime).thenReturn(mockUtcTime);
            timezoneUtilMock.when(() -> TimezoneUtil.convertUtcToBrasilia(mockUtcTime))
                    .thenReturn("15/01/2024 08:30:00");

            // Act
            Evento result = eventoService.criarEvento(evento);

            // Assert
            assertNotNull(result);
            assertEquals("61996593711", result.getPhoneNumber()); // Should remain unchanged
        }
    }

    @Test
    void testCriarEvento_WithMultipleLeadingZeros_ShouldRemoveAll() {
        // Arrange
        Evento evento = new Evento();
        evento.setDescription("Test call");
        evento.setEventType("CALL_RECEIVED");
        evento.setDeviceId("test-device");
        evento.setAdditionalData("{\"numero\": \"0061996593711\"}");

        Evento savedEvento = new Evento();
        savedEvento.setId(1L);
        savedEvento.setDescription("Test call");
        savedEvento.setEventType("CALL_RECEIVED");
        savedEvento.setDeviceId("test-device");
        savedEvento.setPhoneNumber("61996593711"); // Both leading zeros should be removed
        savedEvento.setTimestamp(mockUtcTime);

        when(eventoRepository.save(any(Evento.class))).thenReturn(savedEvento);

        try (MockedStatic<TimezoneUtil> timezoneUtilMock = mockStatic(TimezoneUtil.class)) {
            timezoneUtilMock.when(TimezoneUtil::getCurrentUtcTime).thenReturn(mockUtcTime);
            timezoneUtilMock.when(() -> TimezoneUtil.convertUtcToBrasilia(mockUtcTime))
                    .thenReturn("15/01/2024 08:30:00");

            // Act
            Evento result = eventoService.criarEvento(evento);

            // Assert
            assertNotNull(result);
            assertEquals("61996593711", result.getPhoneNumber()); // All leading zeros should be removed
        }
    }

    @Test
    void testCriarEvento_WithLongNumber_ShouldTruncateTo11Digits() {
        // Arrange
        Evento evento = new Evento();
        evento.setDescription("Test call");
        evento.setEventType("CALL_RECEIVED");
        evento.setDeviceId("test-device");
        evento.setAdditionalData("{\"numero\": \"061996593711999\"}");

        Evento savedEvento = new Evento();
        savedEvento.setId(1L);
        savedEvento.setDescription("Test call");
        savedEvento.setEventType("CALL_RECEIVED");
        savedEvento.setDeviceId("test-device");
        savedEvento.setPhoneNumber("61996593711"); // Should be truncated to 11 digits
        savedEvento.setTimestamp(mockUtcTime);

        when(eventoRepository.save(any(Evento.class))).thenReturn(savedEvento);

        try (MockedStatic<TimezoneUtil> timezoneUtilMock = mockStatic(TimezoneUtil.class)) {
            timezoneUtilMock.when(TimezoneUtil::getCurrentUtcTime).thenReturn(mockUtcTime);
            timezoneUtilMock.when(() -> TimezoneUtil.convertUtcToBrasilia(mockUtcTime))
                    .thenReturn("15/01/2024 08:30:00");

            // Act
            Evento result = eventoService.criarEvento(evento);

            // Assert
            assertNotNull(result);
            assertEquals("61996593711", result.getPhoneNumber()); // Should be truncated to 11 digits
        }
    }

    @Test
    void testBuscarPorId_ShouldReturnEvento() {
        // Arrange
        Long id = 1L;
        Evento expectedEvento = new Evento();
        expectedEvento.setId(id);
        when(eventoRepository.findById(id)).thenReturn(Optional.of(expectedEvento));

        // Act
        Optional<Evento> result = eventoService.buscarPorId(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedEvento, result.get());
    }
}
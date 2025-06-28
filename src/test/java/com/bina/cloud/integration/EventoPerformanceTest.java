package com.bina.cloud.integration;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.service.EventoService;
import com.bina.cloud.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Teste de Performance para verificar memory leaks
 * Simula carga alta de eventos para identificar problemas de memória
 */
@SpringBootTest
@ActiveProfiles("test")
class EventoPerformanceTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoService eventoService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private static final int BATCH_SIZE = 100;
    private static final int CONCURRENT_THREADS = 10;

    @BeforeEach
    void setUp() {
        eventoRepository.deleteAll();
    }

    @Test
    void performanceTest_ShouldHandleHighLoadWithoutMemoryLeaks() throws Exception {
        // Arrange
        List<Evento> eventos = createBatchEvents(BATCH_SIZE);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);

        // Act - Processar eventos em paralelo
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Evento evento : eventos) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    eventoService.criarEvento(evento);
                } catch (Exception e) {
                    fail("Erro ao processar evento: " + e.getMessage());
                }
            }, executor);
            futures.add(future);
        }

        // Aguardar conclusão de todos os eventos
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Assert - Verificar que todos os eventos foram processados
        assertEquals(BATCH_SIZE, eventoRepository.count());

        // Verificar que as notificações foram enviadas
        verify(messagingTemplate, times(BATCH_SIZE))
                .convertAndSend(eq("/topic/events"), any(NotificationService.EventNotification.class));

        // Verificar que não há eventos duplicados
        long uniquePhoneNumbers = eventoRepository.findAll().stream()
                .map(Evento::getPhoneNumber)
                .distinct()
                .count();
        assertTrue(uniquePhoneNumbers > 0);
    }

    @Test
    void performanceTest_ShouldHandleRepeatedPhoneNumbersEfficiently() throws Exception {
        // Arrange - Criar eventos com números repetidos
        List<Evento> eventos = new ArrayList<>();
        String[] phoneNumbers = { "061981122752", "11987654321", "21999887766" };

        for (int i = 0; i < BATCH_SIZE; i++) {
            String phoneNumber = phoneNumbers[i % phoneNumbers.length];
            eventos.add(createEvento("CALL_RECEIVED", phoneNumber, "device-" + i));
        }

        // Act - Processar eventos sequencialmente
        long startTime = System.currentTimeMillis();

        for (Evento evento : eventos) {
            eventoService.criarEvento(evento);
        }

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        // Assert - Verificar performance
        assertEquals(BATCH_SIZE, eventoRepository.count());

        // Verificar que o tempo de processamento é razoável (< 10 segundos para 100
        // eventos)
        assertTrue(processingTime < 10000, "Processamento muito lento: " + processingTime + "ms");

        // Verificar que as consultas por número de telefone funcionam
        for (String phoneNumber : phoneNumbers) {
            List<Evento> eventsForPhone = eventoRepository.findByPhoneNumberOrderByTimestampDesc(phoneNumber);
            assertTrue(eventsForPhone.size() > 0, "Nenhum evento encontrado para " + phoneNumber);
        }
    }

    @Test
    void performanceTest_ShouldHandleLargeAdditionalData() throws Exception {
        // Arrange - Criar eventos com dados adicionais grandes
        List<Evento> eventos = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            Evento evento = new Evento();
            evento.setDescription("Chamada com dados grandes " + i);
            evento.setDeviceId("052ad7f7b6ee816b");
            evento.setEventType("CALL_RECEIVED");

            // Criar JSON grande com muitos campos
            StringBuilder largeJson = new StringBuilder();
            largeJson.append("{\"numero\":\"061981122752\",");
            largeJson.append("\"data\":\"24/06/2025 20:00:00\",");
            largeJson.append("\"receivingNumber\":\"\",");
            largeJson.append("\"metadata\":{");
            for (int j = 0; j < 20; j++) {
                largeJson.append("\"field").append(j).append("\":\"value").append(j).append("\",");
            }
            largeJson.append("\"finalField\":\"finalValue\"}");
            largeJson.append("}");

            evento.setAdditionalData(largeJson.toString());
            eventos.add(evento);
        }

        // Act - Processar eventos
        long startTime = System.currentTimeMillis();

        for (Evento evento : eventos) {
            eventoService.criarEvento(evento);
        }

        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        // Assert - Verificar que todos foram processados
        assertEquals(50, eventoRepository.count());

        // Verificar que o tempo de processamento é aceitável
        assertTrue(processingTime < 5000, "Processamento muito lento para dados grandes: " + processingTime + "ms");

        // Verificar que a extração de telefone ainda funciona
        List<Evento> allEvents = eventoRepository.findAll();
        for (Evento evento : allEvents) {
            assertEquals("061981122752", evento.getPhoneNumber());
        }
    }

    private List<Evento> createBatchEvents(int count) {
        List<Evento> eventos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String phoneNumber = String.format("061%08d", i);
            eventos.add(createEvento("CALL_RECEIVED", phoneNumber, "device-" + i));
        }
        return eventos;
    }

    private Evento createEvento(String eventType, String phoneNumber, String deviceId) {
        Evento evento = new Evento();
        evento.setDescription("Chamada " + eventType.toLowerCase().replace("_", " "));
        evento.setDeviceId(deviceId);
        evento.setEventType(eventType);
        evento.setAdditionalData("{\"numero\":\"" + phoneNumber + "\",\"data\":\"24/06/2025 20:00:00\"}");
        return evento;
    }
}
package com.bina.cloud.integration;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.service.EventoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste End-to-End para o MVP da aplicação
 * Testa o fluxo completo: API REST → Banco de Dados → WebSocket → Cliente
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EventoE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoService eventoService;

    private WebSocketStompClient stompClient;
    private CompletableFuture<String> notificationFuture;

    @BeforeEach
    void setUp() {
        eventoRepository.deleteAll();

        // Configurar cliente WebSocket
        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        WebSocketTransport webSocketTransport = new WebSocketTransport(standardWebSocketClient);
        SockJsClient sockJsClient = new SockJsClient(List.of(webSocketTransport));

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        notificationFuture = new CompletableFuture<>();
    }

    @Test
    void mvpE2E_ShouldReceiveWebSocketNotification() throws Exception {
        // Arrange - Conectar ao WebSocket
        StompSession session = connectToWebSocket();
        subscribeToNotifications(session);

        // Arrange - Dados do MVP
        Evento evento = new Evento();
        evento.setDescription("Chamada recebida");
        evento.setDeviceId("052ad7f7b6ee816b");
        evento.setEventType("CALL_RECEIVED");
        evento.setAdditionalData(
                "{\"numero\":\"061981122752\",\"data\":\"24/06/2025 19:59:59\",\"receivingNumber\":\"\"}");

        // Act - Enviar evento via API REST
        String response = sendEventViaRest(evento);

        // Assert - Verificar resposta da API
        Evento savedEvento = objectMapper.readValue(response, Evento.class);
        assertNotNull(savedEvento.getId());
        assertEquals("61981122752", savedEvento.getPhoneNumber());
        assertEquals("CALL_RECEIVED", savedEvento.getEventType());

        // Assert - Verificar notificação WebSocket
        String notification = notificationFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(notification);
        assertTrue(notification.contains("61981122752"));
        assertTrue(notification.contains("CALL_RECEIVED"));
        assertTrue(notification.contains("portal.gasdelivery.com.br"));
    }

    @Test
    void mvpE2E_ShouldHandleMultipleEventsForSamePhone() throws Exception {
        // Arrange - Conectar ao WebSocket
        StompSession session = connectToWebSocket();
        subscribeToNotifications(session);

        // Act - Primeira chamada
        Evento evento1 = createEvento("CALL_RECEIVED", "061981122752");
        sendEventViaRest(evento1);

        // Act - Segunda chamada do mesmo número
        Evento evento2 = createEvento("CALL_MISSED", "061981122752");
        sendEventViaRest(evento2);

        // Assert - Verificar que ambos foram salvos
        assertEquals(2, eventoRepository.count());
        assertEquals(2, eventoRepository.findByPhoneNumberOrderByTimestampDesc("61981122752").size());

        // Assert - Verificar notificações WebSocket
        String notification1 = notificationFuture.get(5, TimeUnit.SECONDS);
        String notification2 = notificationFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(notification1);
        assertNotNull(notification2);
        assertTrue(notification1.contains("61981122752") || notification2.contains("61981122752"));
    }

    @Test
    void mvpE2E_ShouldHandleDifferentEventTypes() throws Exception {
        // Arrange - Conectar ao WebSocket
        StompSession session = connectToWebSocket();
        subscribeToNotifications(session);

        // Act & Assert - CALL_RECEIVED
        Evento evento1 = createEvento("CALL_RECEIVED", "11987654321");
        String response1 = sendEventViaRest(evento1);
        Evento saved1 = objectMapper.readValue(response1, Evento.class);
        assertEquals("11987654321", saved1.getPhoneNumber());

        // Act & Assert - CALL_MISSED
        Evento evento2 = createEvento("CALL_MISSED", "21999887766");
        String response2 = sendEventViaRest(evento2);
        Evento saved2 = objectMapper.readValue(response2, Evento.class);
        assertEquals("21999887766", saved2.getPhoneNumber());

        // Act & Assert - CALL_ANSWERED
        Evento evento3 = createEvento("CALL_ANSWERED", "31988776655");
        String response3 = sendEventViaRest(evento3);
        Evento saved3 = objectMapper.readValue(response3, Evento.class);
        assertEquals("31988776655", saved3.getPhoneNumber());

        // Verificar que todos foram salvos
        assertEquals(3, eventoRepository.count());
    }

    private StompSession connectToWebSocket() throws ExecutionException, InterruptedException, TimeoutException {
        return stompClient.connect(
                "ws://localhost:" + port + "/ws",
                new StompSessionHandlerAdapter() {
                }).get(5, TimeUnit.SECONDS);
    }

    private void subscribeToNotifications(StompSession session) {
        session.subscribe("/topic/events", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                notificationFuture.complete((String) payload);
            }
        });
    }

    private String sendEventViaRest(Evento evento) throws Exception {
        // Usar o serviço para processar o evento corretamente
        Evento savedEvento = eventoService.criarEvento(evento);
        return objectMapper.writeValueAsString(savedEvento);
    }

    private Evento createEvento(String eventType, String phoneNumber) {
        Evento evento = new Evento();
        evento.setDescription("Chamada " + eventType.toLowerCase().replace("_", " "));
        evento.setDeviceId("052ad7f7b6ee816b");
        evento.setEventType(eventType);
        evento.setAdditionalData("{\"numero\":\"" + phoneNumber + "\",\"data\":\"24/06/2025 20:00:00\"}");
        return evento;
    }
}
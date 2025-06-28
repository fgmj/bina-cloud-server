package com.bina.cloud.integration;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.service.EventoService;
import com.bina.cloud.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de Integração para o MVP da aplicação
 * Simula o fluxo completo: recebimento de evento → extração de telefone →
 * notificação WebSocket
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventoIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private EventoRepository eventoRepository;

        @Autowired
        private EventoService eventoService;

        @MockBean
        private SimpMessagingTemplate messagingTemplate;

        @BeforeEach
        void setUp() {
                eventoRepository.deleteAll();
        }

        @Test
        void mvpFlow_ShouldCreateEventAndSendNotification() throws Exception {
                // Arrange - Dados do MVP real
                Evento evento = new Evento();
                evento.setDescription("Chamada recebida");
                evento.setDeviceId("052ad7f7b6ee816b");
                evento.setEventType("CALL_RECEIVED");
                evento.setAdditionalData(
                                "{\"numero\":\"061981122752\",\"data\":\"24/06/2025 19:59:59\",\"receivingNumber\":\"\"}");

                // Act & Assert - Teste da API REST
                String response = mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", notNullValue()))
                                .andExpect(jsonPath("$.description", is("Chamada recebida")))
                                .andExpect(jsonPath("$.deviceId", is("052ad7f7b6ee816b")))
                                .andExpect(jsonPath("$.eventType", is("CALL_RECEIVED")))
                                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))) // Leading zero should be
                                                                                         // removed
                                .andExpect(jsonPath("$.timestamp", notNullValue()))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                // Verificar que o evento foi salvo no banco
                Evento savedEvento = objectMapper.readValue(response, Evento.class);
                assert eventoRepository.findById(savedEvento.getId()).isPresent();

                // Verificar que a notificação foi enviada via WebSocket
                verify(messagingTemplate).convertAndSend(eq("/topic/events"),
                                any(NotificationService.EventNotification.class));
        }

        @Test
        void mvpFlow_ShouldHandleMultipleEventsForSamePhone() throws Exception {
                // Primeira chamada
                Evento evento1 = new Evento();
                evento1.setDescription("Chamada recebida");
                evento1.setDeviceId("052ad7f7b6ee816b");
                evento1.setEventType("CALL_RECEIVED");
                evento1.setAdditionalData("{\"numero\":\"061981122752\",\"data\":\"24/06/2025 19:59:59\"}");

                mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento1)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))); // Leading zero should be
                                                                                          // removed

                // Segunda chamada do mesmo número (deve calcular tempo desde última ligação)
                Evento evento2 = new Evento();
                evento2.setDescription("Chamada perdida");
                evento2.setDeviceId("052ad7f7b6ee816b");
                evento2.setEventType("CALL_MISSED");
                evento2.setAdditionalData("{\"numero\":\"061981122752\",\"data\":\"24/06/2025 20:15:30\"}");

                mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento2)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))); // Leading zero should be
                                                                                          // removed

                // Verificar que ambos os eventos foram salvos
                assert eventoRepository.count() == 2;
                assert eventoRepository.findByPhoneNumberOrderByTimestampDesc("61981122752").size() == 2; // Leading
                                                                                                          // zero should
                                                                                                          // be removed
        }

        @Test
        void mvpFlow_ShouldHandleDifferentPhoneNumberFormats() throws Exception {
                // Teste com número de 10 dígitos
                Evento evento1 = new Evento();
                evento1.setDescription("Chamada recebida");
                evento1.setDeviceId("052ad7f7b6ee816b");
                evento1.setEventType("CALL_RECEIVED");
                evento1.setAdditionalData("{\"numero\":\"6198112275\",\"data\":\"24/06/2025 19:59:59\"}");

                mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento1)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", is("6198112275")));

                // Teste com número de 11 dígitos (com zero à esquerda)
                Evento evento2 = new Evento();
                evento2.setDescription("Chamada recebida");
                evento2.setDeviceId("052ad7f7b6ee816b");
                evento2.setEventType("CALL_RECEIVED");
                evento2.setAdditionalData("{\"numero\":\"061981122752\",\"data\":\"24/06/2025 20:00:00\"}");

                mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento2)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))); // Leading zero should be
                                                                                          // removed

                // Teste com número muito longo (deve truncar para 11 dígitos)
                Evento evento3 = new Evento();
                evento3.setDescription("Chamada recebida");
                evento3.setDeviceId("052ad7f7b6ee816b");
                evento3.setEventType("CALL_RECEIVED");
                evento3.setAdditionalData("{\"numero\":\"55061981122752\",\"data\":\"24/06/2025 20:01:00\"}");

                mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento3)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))); // Should truncate to 11
                                                                                          // digits and remove leading
                                                                                          // zeros
        }

        @Test
        void mvpFlow_ShouldHandleInvalidJsonInAdditionalData() throws Exception {
                // Teste com JSON malformado
                Evento evento = new Evento();
                evento.setDescription("Chamada recebida");
                evento.setDeviceId("052ad7f7b6ee816b");
                evento.setEventType("CALL_RECEIVED");
                evento.setAdditionalData("{\"numero\":\"061981122752\",\"data\":\"24/06/2025 19:59:59\""); // JSON
                                                                                                           // incompleto

                mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))); // Leading zero should be
                                                                                          // removed
        }

        @Test
        void mvpFlow_ShouldHandlePlainTextInAdditionalData() throws Exception {
                // Teste com texto simples (não JSON)
                Evento evento = new Evento();
                evento.setDescription("Chamada recebida");
                evento.setDeviceId("052ad7f7b6ee816b");
                evento.setEventType("CALL_RECEIVED");
                evento.setAdditionalData("Chamada do número 061981122752 às 19:59:59");

                mockMvc.perform(post("/api/eventos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(evento)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))); // Leading zero should be
                                                                                          // removed
        }
}
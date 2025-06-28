package com.bina.cloud.controller;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventoRepository eventoRepository;

    @BeforeEach
    void setUp() {
        eventoRepository.deleteAll();
    }

    private Evento createTestEvento() {
        Evento evento = new Evento();
        evento.setDescription("Test Event");
        evento.setDeviceId("test-device-001");
        evento.setEventType("TEST");
        evento.setAdditionalData("{\"test\": \"data\"}");
        return evento;
    }

    @Test
    void createEvento_ShouldCreateEventoAndReturnCreatedEvento() throws Exception {
        Evento evento = createTestEvento();

        mockMvc.perform(post("/api/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description", is("Test Event")))
                .andExpect(jsonPath("$.deviceId", is("test-device-001")))
                .andExpect(jsonPath("$.eventType", is("TEST")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void createEvento_MVP_ShouldCreateCallReceivedEventWithPhoneNumber() throws Exception {
        // MVP Scenario: Recebimento de chamada com número de telefone
        Evento evento = new Evento();
        evento.setDescription("Chamada recebida");
        evento.setDeviceId("052ad7f7b6ee816b");
        evento.setEventType("CALL_RECEIVED");
        evento.setAdditionalData(
                "{\"numero\":\"061981122752\",\"data\":\"24/06/2025 19:59:59\",\"receivingNumber\":\"\"}");

        mockMvc.perform(post("/api/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description", is("Chamada recebida")))
                .andExpect(jsonPath("$.deviceId", is("052ad7f7b6ee816b")))
                .andExpect(jsonPath("$.eventType", is("CALL_RECEIVED")))
                .andExpect(jsonPath("$.additionalData", containsString("061981122752")))
                .andExpect(jsonPath("$.phoneNumber", is("61981122752"))) // Leading zero should be removed
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void createEvento_CallMissed_ShouldExtractPhoneNumberCorrectly() throws Exception {
        Evento evento = new Evento();
        evento.setDescription("Chamada perdida");
        evento.setDeviceId("052ad7f7b6ee816b");
        evento.setEventType("CALL_MISSED");
        evento.setAdditionalData("{\"numero\":\"11987654321\",\"data\":\"24/06/2025 20:15:30\"}");

        mockMvc.perform(post("/api/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber", is("11987654321")))
                .andExpect(jsonPath("$.eventType", is("CALL_MISSED")));
    }

    @Test
    void createEvento_CallAnswered_ShouldExtractPhoneNumberCorrectly() throws Exception {
        Evento evento = new Evento();
        evento.setDescription("Chamada atendida");
        evento.setDeviceId("052ad7f7b6ee816b");
        evento.setEventType("CALL_ANSWERED");
        evento.setAdditionalData("{\"numero\":\"21999887766\",\"data\":\"24/06/2025 20:30:45\"}");

        mockMvc.perform(post("/api/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber", is("21999887766")))
                .andExpect(jsonPath("$.eventType", is("CALL_ANSWERED")));
    }

    @Test
    void createEvento_InvalidPhoneNumber_ShouldHandleGracefully() throws Exception {
        Evento evento = new Evento();
        evento.setDescription("Chamada com número inválido");
        evento.setDeviceId("052ad7f7b6ee816b");
        evento.setEventType("CALL_RECEIVED");
        evento.setAdditionalData("{\"numero\":\"N/A\",\"data\":\"24/06/2025 20:45:12\"}");

        mockMvc.perform(post("/api/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber", is(""))) // Número vazio para N/A
                .andExpect(jsonPath("$.eventType", is("CALL_RECEIVED")));
    }

    @Test
    void createEvento_NoAdditionalData_ShouldHandleGracefully() throws Exception {
        Evento evento = new Evento();
        evento.setDescription("Chamada sem dados adicionais");
        evento.setDeviceId("052ad7f7b6ee816b");
        evento.setEventType("CALL_RECEIVED");
        evento.setAdditionalData(null);

        mockMvc.perform(post("/api/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber", is("")))
                .andExpect(jsonPath("$.eventType", is("CALL_RECEIVED")));
    }

    @Test
    void getAllEventos_ShouldReturnEmptyList_WhenNoEventosExist() throws Exception {
        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllEventos_ShouldReturnEventosList_WhenEventosExist() throws Exception {
        Evento evento = createTestEvento();
        evento.setTimestamp(LocalDateTime.now());
        eventoRepository.save(evento);

        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Test Event")));
    }

    @Test
    void getEventoById_ShouldReturnEvento_WhenEventoExists() throws Exception {
        Evento evento = createTestEvento();
        evento.setTimestamp(LocalDateTime.now());
        Evento savedEvento = eventoRepository.save(evento);

        mockMvc.perform(get("/api/eventos/{id}", savedEvento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedEvento.getId().intValue())))
                .andExpect(jsonPath("$.description", is("Test Event")));
    }

    @Test
    void getEventoById_ShouldReturnNotFound_WhenEventoDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/eventos/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
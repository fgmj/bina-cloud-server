package com.bina.cloud.controller;

import com.bina.cloud.model.EventType;
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
        evento.setEventType(EventType.BUSY);
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
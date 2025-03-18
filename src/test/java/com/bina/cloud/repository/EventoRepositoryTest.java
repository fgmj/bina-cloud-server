package com.bina.cloud.repository;

import com.bina.cloud.model.EventType;
import com.bina.cloud.model.Evento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EventoRepositoryTest {

    @Autowired
    private EventoRepository eventoRepository;

    private Evento createTestEvento() {
        Evento evento = new Evento();
        evento.setDescription("Test Event");
        evento.setDeviceId("tesasdft-device-001");
        evento.setEventType(EventType.ANSWERED);
        evento.setTimestamp(LocalDateTime.now());
        evento.setAdditionalData("{\"test\": \"data\"}");
        return evento;
    }

    @Test
    void save_ShouldPersistEvento() {
        // Arrange
        Evento evento = createTestEvento();

        // Act
        Evento savedEvento = eventoRepository.save(evento);

        // Assert
        assertThat(savedEvento.getId()).isNotNull();
        assertThat(savedEvento.getDescription()).isEqualTo("Test Event");
        assertThat(savedEvento.getDeviceId()).isEqualTo("test-device-001");
        assertThat(savedEvento.getEventType()).isEqualTo("TEST");
        assertThat(savedEvento.getTimestamp()).isNotNull();
        assertThat(savedEvento.getAdditionalData()).isEqualTo("{\"test\": \"data\"}");
    }

    @Test
    void findById_ShouldReturnEvento_WhenEventoExists() {
        // Arrange
        Evento evento = createTestEvento();
        Evento savedEvento = eventoRepository.save(evento);

        // Act
        Optional<Evento> foundEvento = eventoRepository.findById(savedEvento.getId());

        // Assert
        assertThat(foundEvento).isPresent();
        assertThat(foundEvento.get().getDescription()).isEqualTo("Test Event");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenEventoDoesNotExist() {
        // Act
        Optional<Evento> foundEvento = eventoRepository.findById(999L);

        // Assert
        assertThat(foundEvento).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllEventos() {
        // Arrange
        Evento evento1 = createTestEvento();
        evento1.setDescription("Test Event 1");

        Evento evento2 = createTestEvento();
        evento2.setDescription("Test Event 2");
        evento2.setDeviceId("test-device-002");

        eventoRepository.saveAll(List.of(evento1, evento2));

        // Act
        List<Evento> eventos = eventoRepository.findAll();

        // Assert
        assertThat(eventos).hasSize(2);
        assertThat(eventos).extracting("description")
                .containsExactlyInAnyOrder("Test Event 1", "Test Event 2");
    }

    @Test
    void deleteById_ShouldRemoveEvento() {
        // Arrange
        Evento evento = createTestEvento();
        Evento savedEvento = eventoRepository.save(evento);

        // Act
        eventoRepository.deleteById(savedEvento.getId());

        // Assert
        Optional<Evento> deletedEvento = eventoRepository.findById(savedEvento.getId());
        assertThat(deletedEvento).isEmpty();
    }
}
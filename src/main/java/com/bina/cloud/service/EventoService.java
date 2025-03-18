package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final EventoRepository eventoRepository;
    private final NotificationService notificationService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Transactional
    public Evento criarEvento(Evento evento) {
        log.info("Criando novo evento: {}", evento);
        evento.setTimestamp(LocalDateTime.now());
        Evento eventoSalvo = eventoRepository.save(evento);
        log.info("Evento salvo com sucesso: {}", eventoSalvo);

        // Notificar via WebSocket
        notificationService.notifyNewEvent(
                eventoSalvo.getId().toString(),
                eventoSalvo.getDescription(),
                eventoSalvo.getEventType().toString(),
                eventoSalvo.getDeviceId(),
                eventoSalvo.getTimestamp().format(formatter),
                eventoSalvo.getAdditionalData());

        return eventoSalvo;
    }

    public List<Evento> listarEventos() {
        log.info("Listando todos os eventos");
        List<Evento> eventos = eventoRepository.findAll();
        log.info("Total de eventos encontrados: {}", eventos.size());
        return eventos;
    }

    public Optional<Evento> buscarPorId(Long id) {
        log.info("Buscando evento por ID: {}", id);
        Optional<Evento> evento = eventoRepository.findById(id);
        log.info("Evento encontrado: {}", evento.isPresent());
        return evento;
    }
}
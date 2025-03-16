package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final NotificationService notificationService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Transactional
    public Evento criarEvento(Evento evento) {
        evento.setTimestamp(LocalDateTime.now());
        Evento eventoSalvo = eventoRepository.save(evento);
        
        // Notificar via WebSocket
        notificationService.notifyNewEvent(
            eventoSalvo.getId().toString(),
            eventoSalvo.getDescription(),
            eventoSalvo.getEventType(),
            eventoSalvo.getDeviceId(),
            eventoSalvo.getTimestamp().format(formatter),
            eventoSalvo.getAdditionalData()
        );
        
        return eventoSalvo;
    }

    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    public Optional<Evento> buscarPorId(Long id) {
        return eventoRepository.findById(id);
    }
} 
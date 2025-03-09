package com.bina.cloud.controller;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
public class EventoController {

    private final EventoRepository eventoRepository;

    @PostMapping
    public ResponseEntity<Evento> createEvento(@RequestBody Evento evento) {
        log.info("Received new event: {}", evento);
        evento.setTimestamp(LocalDateTime.now());
        Evento savedEvento = eventoRepository.save(evento);
        return ResponseEntity.ok(savedEvento);
    }

    @GetMapping
    public ResponseEntity<List<Evento>> getAllEventos() {
        return ResponseEntity.ok(eventoRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evento> getEventoById(@PathVariable Long id) {
        return eventoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 
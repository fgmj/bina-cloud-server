package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final EventoRepository eventoRepository;
    private final WebSocketService webSocketService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    @Autowired
    private DispositivoService dispositivoService;

    @Transactional
    public Evento criarEvento(Evento evento) {
        log.info("Criando novo evento: {}", evento);
        if (evento.getTimestamp() == null) {
            evento.setTimestamp(LocalDateTime.now());
        }
        Evento eventoSalvo = eventoRepository.save(evento);
        log.info("Evento salvo com sucesso: {}", eventoSalvo);

        // Atualizar última conexão do dispositivo
        dispositivoService.atualizarUltimaConexao(evento.getDeviceId());

        // Notificar via WebSocket
        webSocketService.notifyNewEvent(eventoSalvo);

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
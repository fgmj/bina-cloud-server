package com.bina.cloud.controller;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.service.EventoService;
import com.bina.cloud.util.TimezoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final EventoRepository eventoRepository;
    private final EventoService eventoService;

    @GetMapping("/eventos")
    public String eventos(Model model) {
        long startTime = System.currentTimeMillis();
        log.debug("[WebController] eventos - IN");
        try {
            List<Evento> eventos = eventoRepository.findAll(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent();

            // Converter timestamps UTC para Brasília
            List<Evento> eventosComTimezone = eventos.stream()
                    .map(this::convertEventoTimezone)
                    .collect(Collectors.toList());

            model.addAttribute("eventos", eventosComTimezone);

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("[WebController] eventos - OUT size={} durationMs={}ms", eventosComTimezone.size(), durationMs);
            return "eventos";
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[WebController] eventos - ERROR durationMs={}ms message={}", durationMs, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/monitor")
    public String monitor(Model model) {
        long startTime = System.currentTimeMillis();
        log.debug("[WebController] monitor - IN");
        try {
            List<Evento> eventos = eventoService.getUltimosEventos(50);

            // Converter timestamps UTC para Brasília
            List<Evento> eventosComTimezone = eventos.stream()
                    .map(this::convertEventoTimezone)
                    .collect(Collectors.toList());

            model.addAttribute("eventos", eventosComTimezone);

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("[WebController] monitor - OUT size={} durationMs={}ms", eventosComTimezone.size(), durationMs);
            return "monitor";
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[WebController] monitor - ERROR durationMs={}ms message={}", durationMs, e.getMessage(), e);
            throw e;
        }
    }

    @MessageMapping("/heartbeat")
    @SendTo("/topic/heartbeat")
    public Map<String, Object> heartbeat(Map<String, Object> message) {
        long startTime = System.currentTimeMillis();
        log.debug("[WebController] heartbeat - IN timestamp={}", message.get("timestamp"));
        try {
            Map<String, Object> response = Map.of(
                    "timestamp", message.get("timestamp"),
                    "serverTime", System.currentTimeMillis(),
                    "status", "ok");

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("[WebController] heartbeat - OUT durationMs={}ms", durationMs);
            return response;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[WebController] heartbeat - ERROR durationMs={}ms message={}", durationMs, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converte o timestamp do evento de UTC para Brasília
     */
    private Evento convertEventoTimezone(Evento evento) {
        if (evento.getTimestamp() != null) {
            // Criar uma cópia do evento com timestamp convertido
            Evento eventoConvertido = new Evento();
            eventoConvertido.setId(evento.getId());
            eventoConvertido.setDescription(evento.getDescription());
            eventoConvertido.setDeviceId(evento.getDeviceId());
            eventoConvertido.setEventType(evento.getEventType());
            eventoConvertido.setAdditionalData(evento.getAdditionalData());
            eventoConvertido.setPhoneNumber(evento.getPhoneNumber());

            // Converter timestamp UTC para Brasília
            eventoConvertido.setTimestamp(TimezoneUtil.convertUtcToBrasiliaDateTime(evento.getTimestamp()));

            return eventoConvertido;
        }
        return evento;
    }
}
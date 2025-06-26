package com.bina.cloud.controller;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final EventoRepository eventoRepository;
    private final EventoService eventoService;

    @GetMapping("/eventos")
    public String eventos(Model model) {
        model.addAttribute("eventos",
                eventoRepository.findAll(
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent());
        return "eventos";
    }

    @GetMapping("/monitor")
    public String monitor(Model model) {
        List<Evento> eventos = eventoService.getUltimosEventos(50);
        model.addAttribute("eventos", eventos);
        return "monitor";
    }

    @MessageMapping("/heartbeat")
    @SendTo("/topic/heartbeat")
    public Map<String, Object> heartbeat(Map<String, Object> message) {
        // Echo back the timestamp to confirm receipt
        return Map.of(
                "timestamp", message.get("timestamp"),
                "serverTime", System.currentTimeMillis(),
                "status", "ok");
    }
}
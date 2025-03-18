package com.bina.cloud.controller;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final EventoRepository eventoRepository;

    @GetMapping("/eventos")
    public String eventos(Model model) {
        model.addAttribute("eventos", 
            eventoRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))
            ).getContent()
        );
        return "eventos";
    }

    @GetMapping("/monitor")
    public String monitor() {
        return "monitor";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }
} 
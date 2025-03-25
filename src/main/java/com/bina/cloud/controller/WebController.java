package com.bina.cloud.controller;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class WebController {
    private final EventoRepository eventoRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("currentPage", "index");
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("currentPage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/monitor")
    public String monitor(Model model) {
        model.addAttribute("currentPage", "monitor");
        return "monitor";
    }

    @GetMapping("/eventos")
    public String eventos(Model model) {
        model.addAttribute("currentPage", "eventos");
        model.addAttribute("eventos",
                eventoRepository.findAll(
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"))).getContent());
        return "eventos";
    }

}
package com.bina.cloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioViewController {

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("currentPage", "usuarios");
        return "usuarios";
    }
}
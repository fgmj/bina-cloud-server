package com.bina.cloud.controller;

import com.bina.cloud.model.Dispositivo;
import com.bina.cloud.service.DispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dispositivos")
@RequiredArgsConstructor
public class DispositivoWebController {

    private final DispositivoService dispositivoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("currentPage", "dispositivos");
        model.addAttribute("dispositivos", dispositivoService.listarTodos());
        return "dispositivos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("currentPage", "dispositivos");
        model.addAttribute("dispositivo", new Dispositivo());
        return "dispositivos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("currentPage", "dispositivos");
        return dispositivoService.buscarPorId(id)
                .map(dispositivo -> {
                    model.addAttribute("dispositivo", dispositivo);
                    return "dispositivos/form";
                })
                .orElse("redirect:/dispositivos");
    }

    @PostMapping
    public String salvar(@ModelAttribute Dispositivo dispositivo, RedirectAttributes redirectAttributes) {
        try {
            if (dispositivo.getId() == null) {
                dispositivoService.salvar(dispositivo);
                redirectAttributes.addFlashAttribute("mensagem", "Dispositivo cadastrado com sucesso!");
            } else {
                dispositivoService.atualizar(dispositivo.getId(), dispositivo);
                redirectAttributes.addFlashAttribute("mensagem", "Dispositivo atualizado com sucesso!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar dispositivo: " + e.getMessage());
        }
        return "redirect:/dispositivos";
    }

    @PatchMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dispositivoService.desativar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Dispositivo desativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao desativar dispositivo: " + e.getMessage());
        }
        return "redirect:/dispositivos";
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dispositivoService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagem", "Dispositivo excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir dispositivo: " + e.getMessage());
        }
        return "redirect:/dispositivos";
    }
}
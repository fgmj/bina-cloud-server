package com.bina.cloud.controller;

import com.bina.cloud.dto.UsuarioDispositivoDTO;
import com.bina.cloud.service.UsuarioDispositivoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios-dispositivos")
@RequiredArgsConstructor
@Slf4j
public class UsuarioDispositivoController {

    private final UsuarioDispositivoService usuarioDispositivoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<UsuarioDispositivoDTO>> listarDispositivosDoUsuario(@PathVariable Long usuarioId) {
        log.info("Recebida requisição para listar dispositivos do usuário: {}", usuarioId);
        return ResponseEntity.ok(usuarioDispositivoService.listarDispositivosDoUsuario(usuarioId));
    }

    @GetMapping("/dispositivo/{dispositivoId}")
    public ResponseEntity<List<UsuarioDispositivoDTO>> listarUsuariosDoDispositivo(@PathVariable Long dispositivoId) {
        log.info("Recebida requisição para listar usuários do dispositivo: {}", dispositivoId);
        return ResponseEntity.ok(usuarioDispositivoService.listarUsuariosDoDispositivo(dispositivoId));
    }

    @PostMapping("/usuario/{usuarioId}/dispositivo/{dispositivoId}")
    public ResponseEntity<?> associarDispositivo(
            @PathVariable Long usuarioId,
            @PathVariable Long dispositivoId) {
        log.info("Recebida requisição para associar dispositivo {} ao usuário {}", dispositivoId, usuarioId);
        try {
            return ResponseEntity.ok(usuarioDispositivoService.associarDispositivo(usuarioId, dispositivoId));
        } catch (RuntimeException e) {
            log.error("Erro ao associar dispositivo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/usuario/{usuarioId}/dispositivo/{dispositivoId}")
    public ResponseEntity<?> removerAssociacao(
            @PathVariable Long usuarioId,
            @PathVariable Long dispositivoId) {
        log.info("Recebida requisição para remover associação entre usuário {} e dispositivo {}", usuarioId,
                dispositivoId);
        try {
            usuarioDispositivoService.removerAssociacao(usuarioId, dispositivoId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Erro ao remover associação: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
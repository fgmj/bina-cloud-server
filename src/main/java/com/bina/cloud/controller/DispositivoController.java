package com.bina.cloud.controller;

import com.bina.cloud.model.Dispositivo;
import com.bina.cloud.service.DispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispositivos")
@RequiredArgsConstructor
public class DispositivoController {

    private final DispositivoService dispositivoService;

    @GetMapping
    public ResponseEntity<List<Dispositivo>> listarTodos() {
        return ResponseEntity.ok(dispositivoService.listarTodos());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Dispositivo>> listarAtivos() {
        return ResponseEntity.ok(dispositivoService.listarAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dispositivo> buscarPorId(@PathVariable String id) {
        return dispositivoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Dispositivo> criar(@RequestBody Dispositivo dispositivo) {
        return ResponseEntity.ok(dispositivoService.salvar(dispositivo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dispositivo> atualizar(@PathVariable String id, @RequestBody Dispositivo dispositivo) {
        try {
            return ResponseEntity.ok(dispositivoService.atualizar(id, dispositivo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        dispositivoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Dispositivo> desativar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(dispositivoService.desativar(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
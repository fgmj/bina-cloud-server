package com.bina.cloud.controller;

import com.bina.cloud.model.Evento;
import com.bina.cloud.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Eventos", description = "API para gerenciamento de eventos")
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @Operation(summary = "Criar novo evento", description = "Cria um novo evento com os dados fornecidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<Evento> createEvento(
            @Parameter(description = "Dados do evento a ser criado", required = true)
            @RequestBody Evento evento) {
        long startTime = System.currentTimeMillis();
        log.info("[EventoController] createEvento - IN deviceId={} eventType={} description={} ",
                evento.getDeviceId(), evento.getEventType(), evento.getDescription());
        try {
            Evento created = eventoService.criarEvento(evento);
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("[EventoController] createEvento - OUT success id={} durationMs={}ms", created.getId(), durationMs);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[EventoController] createEvento - ERROR durationMs={}ms message={}", durationMs, e.getMessage(), e);
            throw e; // Propagar exceção para tratamento global
        }
    }

    @GetMapping
    @Operation(summary = "Listar todos os eventos", description = "Retorna uma lista de todos os eventos cadastrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso")
    })
    public ResponseEntity<List<Evento>> getAllEventos() {
        long startTime = System.currentTimeMillis();
        log.debug("[EventoController] getAllEventos - IN");
        try {
            var eventos = eventoService.listarEventos();
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("[EventoController] getAllEventos - OUT size={} durationMs={}ms", eventos.size(), durationMs);
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[EventoController] getAllEventos - ERROR durationMs={}ms message={}", durationMs, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID", description = "Retorna um evento específico pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<Evento> getEventoById(
            @Parameter(description = "ID do evento a ser buscado", required = true)
            @PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        log.debug("[EventoController] getEventoById - IN id={}", id);
        try {
            var response = eventoService.buscarPorId(id)
                    .map(evento -> {
                        long durationMs = System.currentTimeMillis() - startTime;
                        log.info("[EventoController] getEventoById - OUT FOUND id={} durationMs={}ms", id, durationMs);
                        return ResponseEntity.ok(evento);
                    })
                    .orElseGet(() -> {
                        long durationMs = System.currentTimeMillis() - startTime;
                        log.warn("[EventoController] getEventoById - OUT NOT_FOUND id={} durationMs={}ms", id, durationMs);
                        return ResponseEntity.notFound().build();
                    });
            return response;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("[EventoController] getEventoById - ERROR id={} durationMs={}ms message={}", id, durationMs, e.getMessage(), e);
            throw e;
        }
    }
} 
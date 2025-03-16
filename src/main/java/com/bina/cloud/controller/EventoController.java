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
        log.info("Received new event: {}", evento);
        return ResponseEntity.ok(eventoService.criarEvento(evento));
    }

    @GetMapping
    @Operation(summary = "Listar todos os eventos", description = "Retorna uma lista de todos os eventos cadastrados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de eventos retornada com sucesso")
    })
    public ResponseEntity<List<Evento>> getAllEventos() {
        return ResponseEntity.ok(eventoService.listarEventos());
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
        return eventoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 
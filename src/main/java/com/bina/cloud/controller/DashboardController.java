package com.bina.cloud.controller;

import com.bina.cloud.dto.DashboardStatsDTO;
import com.bina.cloud.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "API para o dashboard de monitoramento")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    @Operation(summary = "Obter estatísticas", description = "Retorna as estatísticas atuais para o dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    })
    public ResponseEntity<DashboardStatsDTO> getStats(@RequestParam(defaultValue = "today") String period) {
        log.debug("Obtendo estatísticas do dashboard para o período: {}", period);
        DashboardStatsDTO stats = dashboardService.getStats(period);
        return ResponseEntity.ok(stats);
    }
}
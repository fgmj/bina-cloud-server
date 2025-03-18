package com.bina.cloud.service;

import com.bina.cloud.dto.*;
import com.bina.cloud.model.Evento;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final EventoService eventoService;
    private final ObjectMapper objectMapper;
    
    public DashboardStatsDTO getStats() {
        List<Evento> eventos = eventoService.listarEventos();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        
        // Filtrar eventos do dia
        List<Evento> eventosHoje = eventos.stream()
                .filter(e -> e.getTimestamp().isAfter(startOfDay))
                .collect(Collectors.toList());
        
        // Calcular métricas básicas
        long totalCalls = eventosHoje.size();
        long answeredCalls = eventosHoje.stream()
                .filter(e -> "CALL_RECEIVED".equals(e.getEventType()))
                .count();
        long missedCalls = totalCalls - answeredCalls;
        double answerRate = totalCalls > 0 ? (answeredCalls * 100.0 / totalCalls) : 0;
        
        return DashboardStatsDTO.builder()
                .totalCalls(totalCalls)
                .answeredCalls(answeredCalls)
                .missedCalls(missedCalls)
                .answerRate(Math.round(answerRate * 10) / 10.0)
                .callsPerHour(calculateCallsPerHour(eventosHoje))
                .deviceStats(calculateDeviceStats(eventos))
                .peakHours(calculatePeakHours(eventos))
                .temporalData(calculateTemporalData(eventos))
                .peakMetrics(calculatePeakMetrics(eventosHoje))
                .recentCalls(getRecentCalls(eventos))
                .build();
    }
    
    private List<Integer> calculateCallsPerHour(List<Evento> eventos) {
        int[] callsPerHour = new int[24];
        eventos.forEach(e -> callsPerHour[e.getTimestamp().getHour()]++);
        return IntStream.of(callsPerHour).boxed().collect(Collectors.toList());
    }
    
    private DeviceStatsDTO calculateDeviceStats(List<Evento> eventos) {
        Map<String, Long> deviceCounts = eventos.stream()
                .collect(Collectors.groupingBy(
                        Evento::getDeviceId,
                        Collectors.counting()
                ));
        
        List<String> labels = new ArrayList<>(deviceCounts.keySet());
        List<Integer> values = deviceCounts.values().stream()
                .map(Long::intValue)
                .collect(Collectors.toList());
        
        return DeviceStatsDTO.builder()
                .labels(labels)
                .values(values)
                .build();
    }
    
    private List<List<Integer>> calculatePeakHours(List<Evento> eventos) {
        // Matriz 5x24 (dias da semana x horas)
        List<List<Integer>> peakHours = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        
        for (int day = 0; day < 5; day++) {
            LocalDate currentDate = monday.plusDays(day);
            int[] hourCounts = new int[24];
            
            eventos.stream()
                    .filter(e -> e.getTimestamp().toLocalDate().equals(currentDate))
                    .forEach(e -> hourCounts[e.getTimestamp().getHour()]++);
            
            peakHours.add(IntStream.of(hourCounts).boxed().collect(Collectors.toList()));
        }
        
        return peakHours;
    }
    
    private List<TemporalDataDTO> calculateTemporalData(List<Evento> eventos) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        
        return eventos.stream()
                .filter(e -> e.getTimestamp().isAfter(startOfWeek))
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().toLocalDate(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> TemporalDataDTO.builder()
                        .timestamp(entry.getKey().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .value(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    private PeakMetricsDTO calculatePeakMetrics(List<Evento> eventosHoje) {
        int currentHour = LocalDateTime.now().getHour();
        long currentHourCalls = eventosHoje.stream()
                .filter(e -> e.getTimestamp().getHour() == currentHour)
                .count();
        
        // Média histórica para a hora atual
        double avgCallsThisHour = eventosHoje.stream()
                .filter(e -> e.getTimestamp().getHour() == currentHour)
                .count();
        
        // Próxima hora com maior probabilidade de pico
        int nextPeakHour = (currentHour + 1) % 24;
        long maxCalls = 0;
        for (int i = 1; i <= 3; i++) {
            int hour = (currentHour + i) % 24;
            long calls = eventosHoje.stream()
                    .filter(e -> e.getTimestamp().getHour() == hour)
                    .count();
            if (calls > maxCalls) {
                maxCalls = calls;
                nextPeakHour = hour;
            }
        }
        
        String comparison = String.format("%.1f%%", 
                avgCallsThisHour > 0 ? (currentHourCalls * 100.0 / avgCallsThisHour) : 100);
        
        return PeakMetricsDTO.builder()
                .currentPeak(String.format("%02d:00 - %d chamadas", currentHour, currentHourCalls))
                .nextPeak(String.format("%02d:00 - %d chamadas previstas", nextPeakHour, maxCalls))
                .comparison(comparison)
                .build();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> parseAdditionalData(String additionalData) {
        try {
            return objectMapper.readValue(additionalData, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing additionalData: {}", additionalData, e);
            return new HashMap<>();
        }
    }
    
    private List<CallDTO> getRecentCalls(List<Evento> eventos) {
        return eventos.stream()
                .sorted(Comparator.comparing(Evento::getTimestamp).reversed())
                .limit(10)
                .map(e -> CallDTO.builder()
                        .phoneNumber(e.getPhoneNumber())
                        .timestamp(e.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .duration(parseAdditionalData(e.getAdditionalData()).getOrDefault("duration", "-"))
                        .status(e.getEventType())
                        .device(e.getDeviceId())
                        .build())
                .collect(Collectors.toList());
    }
} 
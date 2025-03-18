package com.bina.cloud.service;

import com.bina.cloud.dto.*;
import com.bina.cloud.model.Evento;
import com.bina.cloud.model.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

        private final EventoService eventoService;
        private final ObjectMapper objectMapper;

        public DashboardStatsDTO getStats(String period) {
                log.info("Obtendo estatísticas do dashboard para o período: {}", period);
                List<Evento> eventos = eventoService.listarEventos();
                log.info("Total de eventos encontrados: {}", eventos.size());

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = switch (period) {
                        case "7days" -> now.minusDays(7);
                        case "28days" -> now.minusDays(28);
                        default -> now.truncatedTo(ChronoUnit.DAYS); // today
                };

                List<Evento> eventosFiltrados = eventos.stream()
                                .filter(e -> !e.getTimestamp().isBefore(startDate))
                                .collect(Collectors.toList());

                log.info("Eventos no período: {}", eventosFiltrados.size());

                // Calcular métricas
                long totalCalls = eventosFiltrados.size();
                long answeredCalls = eventosFiltrados.stream()
                                .filter(e -> e.getEventType() == EventType.ANSWERED)
                                .count();
                long missedCalls = totalCalls - answeredCalls;
                double answerRate = totalCalls > 0 ? (answeredCalls * 100.0 / totalCalls) : 0;

                log.info("Métricas calculadas: total={}, atendidas={}, perdidas={}, taxa={}%",
                                totalCalls, answeredCalls, missedCalls, Math.round(answerRate * 10) / 10.0);

                DashboardStatsDTO stats = DashboardStatsDTO.builder()
                                .totalCalls(totalCalls)
                                .answeredCalls(answeredCalls)
                                .missedCalls(missedCalls)
                                .answerRate(Math.round(answerRate * 10) / 10.0)
                                .callsPerHour(calculateCallsPerHour(eventosFiltrados))
                                .deviceStats(calculateDeviceStats(eventosFiltrados))
                                .peakHours(calculatePeakHours(eventos))
                                .temporalData(calculateTemporalData(eventosFiltrados))
                                .peakMetrics(calculatePeakMetrics(eventosFiltrados))
                                .recentCalls(getRecentCalls(eventosFiltrados))
                                .build();

                log.info("Estatísticas do dashboard geradas com sucesso");
                return stats;
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
                                                Collectors.counting()));

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
                log.info("Calculando horas de pico");
                log.info("Total de eventos recebidos: {}", eventos.size());

                // Matriz 7x24 (dias da semana x horas)
                List<List<Integer>> peakHours = new ArrayList<>();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = now.minusMonths(3);
                log.info("Calculando horas de pico a partir de {} até {}", startDate, now);

                // Inicializar a matriz com zeros (7 dias x 24 horas)
                for (int day = 0; day < 7; day++) {
                        List<Integer> hourCounts = new ArrayList<>(Collections.nCopies(24, 0));
                        peakHours.add(hourCounts);
                }

                // Filtrar e contar eventos dentro do período
                List<Evento> eventosFiltrados = eventos.stream()
                                .filter(e -> !e.getTimestamp().isBefore(startDate))
                                .collect(Collectors.toList());
                log.info("Total de eventos após filtro de data: {}", eventosFiltrados.size());

                // Encontrar o período real dos dados
                LocalDate minDate = eventosFiltrados.stream()
                                .map(e -> e.getTimestamp().toLocalDate())
                                .min(LocalDate::compareTo)
                                .orElse(now.toLocalDate());
                LocalDate maxDate = eventosFiltrados.stream()
                                .map(e -> e.getTimestamp().toLocalDate())
                                .max(LocalDate::compareTo)
                                .orElse(now.toLocalDate());

                // Calcular o número real de semanas com dados
                long totalWeeks = ChronoUnit.WEEKS.between(minDate, maxDate.plusDays(1));
                if (totalWeeks == 0)
                        totalWeeks = 1;
                log.info("Período real dos dados: {} até {} ({} semanas)", minDate, maxDate, totalWeeks);

                // Agrupar eventos por dia da semana e hora
                eventosFiltrados.forEach(e -> {
                        int dayOfWeek = e.getTimestamp().getDayOfWeek().getValue() - 1; // 0 = Segunda, 6 = Domingo
                        int hour = e.getTimestamp().getHour();
                        List<Integer> dayHours = peakHours.get(dayOfWeek);
                        dayHours.set(hour, dayHours.get(hour) + 1);
                        log.debug("Evento processado: data={}, dia={}, hora={}, total={}",
                                        e.getTimestamp(), dayOfWeek, hour, dayHours.get(hour));
                });

                // Log dos totais antes da média
                log.info("Totais antes da média:");
                for (int day = 0; day < 7; day++) {
                        int total = peakHours.get(day).stream().mapToInt(Integer::intValue).sum();
                        log.info("Dia {}: {} eventos", day, total);
                }

                // Calcular média por semana usando o número real de semanas
                log.info("Usando {} semanas para cálculo da média", totalWeeks);
                for (List<Integer> dayHours : peakHours) {
                        for (int i = 0; i < dayHours.size(); i++) {
                                double media = dayHours.get(i) / (double) totalWeeks;
                                dayHours.set(i, (int) Math.round(media));
                        }
                }

                // Registrar os totais por dia para debug
                String[] diasSemana = { "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo" };
                for (int day = 0; day < 7; day++) {
                        int total = peakHours.get(day).stream().mapToInt(Integer::intValue).sum();
                        log.info("Média semanal de eventos para {}: {}", diasSemana[day], total);

                        // Log detalhado por hora
                        List<Integer> dayHours = peakHours.get(day);
                        StringBuilder hoursLog = new StringBuilder();
                        for (int hour = 0; hour < 24; hour++) {
                                if (dayHours.get(hour) > 0) {
                                        hoursLog.append(String.format("%02dh: %d, ", hour, dayHours.get(hour)));
                                }
                        }
                        if (hoursLog.length() > 0) {
                                log.info("Detalhamento de {} - {}", diasSemana[day], hoursLog);
                        }
                }

                // Retornar apenas os 5 primeiros dias (Segunda a Sexta)
                return peakHours.subList(0, 5);
        }

        private List<TemporalDataDTO> calculateTemporalData(List<Evento> eventos) {
                log.info("Calculando dados temporais");
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startDate = now.minusMonths(3).toLocalDate().atStartOfDay();
                log.info("Período de análise: {} até {}", startDate, now);

                // Primeiro, vamos ver todos os eventos que temos
                log.info("Total de eventos antes do filtro: {}", eventos.size());

                // Criar um mapa com todas as datas no período, inicializando com zero
                Map<LocalDate, Long> eventsByDate = new TreeMap<>();
                LocalDate start = startDate.toLocalDate();
                LocalDate end = now.toLocalDate();
                while (!start.isAfter(end)) {
                        eventsByDate.put(start, 0L);
                        start = start.plusDays(1);
                }

                // Agora contar os eventos por data
                Map<LocalDate, Long> actualEvents = eventos.stream()
                                .filter(e -> !e.getTimestamp().isBefore(startDate))
                                .collect(Collectors.groupingBy(
                                                e -> e.getTimestamp().toLocalDate(),
                                                Collectors.counting()));

                // Mesclar os mapas para garantir que todas as datas tenham um valor
                eventsByDate.putAll(actualEvents);

                List<TemporalDataDTO> temporalData = eventsByDate.entrySet().stream()
                                .map(entry -> {
                                        long timestamp = entry.getKey().atStartOfDay()
                                                        .toInstant(ZoneOffset.UTC).toEpochMilli();
                                        log.debug("Data: {}, Eventos: {}, Timestamp: {}",
                                                        entry.getKey(), entry.getValue(), timestamp);
                                        return TemporalDataDTO.builder()
                                                        .timestamp(timestamp)
                                                        .value(entry.getValue().intValue())
                                                        .build();
                                })
                                .collect(Collectors.toList());

                log.info("Dados temporais calculados: {} registros", temporalData.size());
                return temporalData;
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
                if (additionalData == null) {
                        return new HashMap<>();
                }
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
                                                .duration(parseAdditionalData(e.getAdditionalData())
                                                                .getOrDefault("duration", "-"))
                                                .status(e.getEventType())
                                                .device(e.getDeviceId())
                                                .build())
                                .collect(Collectors.toList());
        }
}
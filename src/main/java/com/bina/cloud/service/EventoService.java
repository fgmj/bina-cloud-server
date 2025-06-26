package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final NotificationService notificationService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final ZoneId brasiliaZone = ZoneId.of("America/Sao_Paulo");
    private final ZoneId utcZone = ZoneId.of("UTC");

    @Transactional
    public Evento criarEvento(Evento evento) {
        // Salvar em UTC no banco de dados
        evento.setTimestamp(LocalDateTime.now(utcZone));

        // Extrair número de telefone do additionalData e salvar no campo phoneNumber
        String phoneNumber = extractPhoneNumber(evento.getAdditionalData());
        evento.setPhoneNumber(phoneNumber);

        Evento eventoSalvo = eventoRepository.save(evento);

        // Converter para Brasília para exibição na notificação
        ZonedDateTime brasiliaTime = eventoSalvo.getTimestamp().atZone(utcZone).withZoneSameInstant(brasiliaZone);

        // Notificar via WebSocket
        notificationService.notifyNewEvent(
                eventoSalvo.getId().toString(),
                eventoSalvo.getDescription(),
                eventoSalvo.getEventType(),
                eventoSalvo.getDeviceId(),
                brasiliaTime.format(formatter),
                eventoSalvo.getAdditionalData());

        return eventoSalvo;
    }

    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    public List<Evento> getUltimosEventos(int limit) {
        return eventoRepository.findTopNByOrderByTimestampDesc(limit);
    }

    public Optional<Evento> buscarPorId(Long id) {
        return eventoRepository.findById(id);
    }

    private String extractPhoneNumber(String additionalData) {
        if (additionalData == null || additionalData.isEmpty()) {
            return "";
        }

        try {
            // Tentar extrair número usando regex para JSON
            String phoneNumber = additionalData.replaceAll(".*\"numero\":\\s*\"([^\"]+)\".*", "$1");

            // Se não encontrou no formato JSON, tentar outros padrões
            if (phoneNumber.equals(additionalData)) {
                // Tentar extrair apenas números
                phoneNumber = additionalData.replaceAll("[^0-9]", "");
            }

            if (!phoneNumber.isEmpty() && !"N/A".equals(phoneNumber)) {
                // Normalizar para 11 dígitos (DDD + número)
                if (phoneNumber.length() > 11) {
                    phoneNumber = phoneNumber.substring(phoneNumber.length() - 11);
                }
                return phoneNumber;
            }

        } catch (Exception e) {
            // Log error silently
        }

        return "";
    }
}
package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import com.bina.cloud.util.TimezoneUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final NotificationService notificationService;

    // Static compiled regex patterns to avoid repeated compilation
    private static final Pattern PHONE_JSON_PATTERN = Pattern.compile(".*\"numero\":\\s*\"([^\"]+)\".*");
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("[^0-9]");

    @Transactional
    public Evento criarEvento(Evento evento) {
        // Salvar em UTC no banco de dados
        evento.setTimestamp(TimezoneUtil.getCurrentUtcTime());

        // Extrair número de telefone do additionalData e salvar no campo phoneNumber
        String phoneNumber = extractPhoneNumber(evento.getAdditionalData());
        evento.setPhoneNumber(phoneNumber);

        Evento eventoSalvo = eventoRepository.save(evento);

        // Converter para Brasília para exibição na notificação
        String brasiliaTime = TimezoneUtil.convertUtcToBrasilia(eventoSalvo.getTimestamp());

        // Notificar via WebSocket
        notificationService.notifyNewEvent(
                eventoSalvo.getId().toString(),
                eventoSalvo.getDescription(),
                eventoSalvo.getEventType(),
                eventoSalvo.getDeviceId(),
                brasiliaTime,
                eventoSalvo.getAdditionalData());

        return eventoSalvo;
    }

    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    public List<Evento> getUltimosEventos(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return eventoRepository.findTopNByOrderByTimestampDesc(pageRequest);
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
            String phoneNumber = PHONE_JSON_PATTERN.matcher(additionalData).replaceAll("$1");

            // Se não encontrou no formato JSON, tentar outros padrões
            if (phoneNumber.equals(additionalData)) {
                // Tentar extrair apenas números
                phoneNumber = DIGITS_ONLY_PATTERN.matcher(additionalData).replaceAll("");
            }

            if (!phoneNumber.isEmpty() && !"N/A".equals(phoneNumber)) {
                // Remover zeros à esquerda (se o número começar com 0, ignorar o 0)
                while (phoneNumber.startsWith("0")) {
                    phoneNumber = phoneNumber.substring(1);
                }

                // Normalizar para 11 dígitos (DDD + número)
                // Só truncar se tiver mais de 11 dígitos
                if (phoneNumber.length() > 11) {
                    phoneNumber = phoneNumber.substring(0, 11);
                }
                return phoneNumber;
            }

        } catch (Exception e) {
            // Log error silently
        }

        return "";
    }
}
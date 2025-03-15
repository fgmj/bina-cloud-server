package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final NotificationService notificationService;

    @Transactional
    public Evento criarEvento(Evento evento) {
        evento.setTimestamp(LocalDateTime.now());
        Evento eventoSalvo = eventoRepository.save(evento);
        
        // Notificar via WebSocket
        String eventUrl = construirEventUrl(eventoSalvo);
        notificationService.notifyNewEvent(
            eventoSalvo.getId().toString(),
            eventoSalvo.getDescription(),
            eventUrl
        );
        
        return eventoSalvo;
    }

    private String construirEventUrl(Evento evento) {
        // Extrair o número de telefone do additionalData
        String phoneNumber = "";
        if (evento.getAdditionalData() != null && !evento.getAdditionalData().isEmpty()) {
            try {
                // Assumindo que additionalData é um JSON com o formato {"numero": "123456789"}
                phoneNumber = evento.getAdditionalData()
                    .replaceAll(".*\"numero\":\\s*\"([^\"]+)\".*", "$1");

                 // Se tiver mais de 11 caracteres, mantém apenas os últimos 11
                if (phoneNumber.length() > 11) {
                    phoneNumber = phoneNumber.substring(phoneNumber.length() - 11);
                }     
            } catch (Exception e) {
                // Se houver erro ao extrair o número, usar string vazia
                phoneNumber = "";
            }
        }
        
        // Construir URL do portal Gas Delivery com o número de telefone
        return String.format(
            "https://portal.gasdelivery.com.br/secure/client/?primary_phone=%s",
            phoneNumber
        );
    }

    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }
} 
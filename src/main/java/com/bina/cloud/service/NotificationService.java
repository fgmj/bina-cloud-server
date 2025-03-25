package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.dto.NotificationDTO;
import com.bina.cloud.dto.WebSocketMessageDTO;
import com.bina.cloud.repository.DispositivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DispositivoRepository dispositivoRepository;

    public void notifyNewEvent(Evento evento) {
        log.info("Notificando novo evento via WebSocket - Evento ID: {}", evento.getId());
        try {
            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setDevices(dispositivoRepository.findAll());

            NotificationDTO notification = new NotificationDTO(
                    evento.getId().toString(),
                    evento.getPhoneNumber(),
                    evento.getEventType().toString(),
                    evento.getDeviceId(),
                    evento.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    evento.getAdditionalData());

            message.setEvent(notification);
            log.debug("Enviando notificação: {}", notification);

            messagingTemplate.convertAndSend("/topic/events", message);
            log.info("Notificação enviada com sucesso");
        } catch (Exception e) {
            log.error("Erro ao enviar notificação - Evento ID: {} - Erro: {}", evento.getId(), e.getMessage(), e);
        }
    }
}
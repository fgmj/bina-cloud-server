package com.bina.cloud.service;

import com.bina.cloud.model.Evento;
import com.bina.cloud.model.Dispositivo;
import com.bina.cloud.dto.NotificationDTO;
import com.bina.cloud.dto.WebSocketMessageDTO;
import com.bina.cloud.repository.DispositivoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final DispositivoRepository dispositivoRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Nova conexão WebSocket estabelecida - ID: {}, URI: {}", session.getId(), session.getUri());
        sessions.put(session.getId(), session);
        log.info("Total de conexões ativas: {}", sessions.size());
        sendInitialData(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        log.info("Conexão WebSocket fechada - ID: {}, Status: {}", session.getId(), status);
        sessions.remove(session.getId());
        log.info("Total de conexões ativas após fechamento: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("Mensagem recebida de {} - Payload: {}", session.getId(), message.getPayload());
    }

    private void sendInitialData(WebSocketSession session) {
        log.info("Enviando dados iniciais para sessão {}", session.getId());
        try {
            List<Dispositivo> dispositivos = dispositivoRepository.findAll();
            log.info("Total de dispositivos encontrados: {}", dispositivos.size());

            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setDevices(dispositivos);

            String jsonMessage = objectMapper.writeValueAsString(message);
            log.debug("Mensagem inicial serializada: {}", jsonMessage);

            session.sendMessage(new TextMessage(jsonMessage));
            log.info("Dados iniciais enviados com sucesso para sessão {}", session.getId());
        } catch (IOException e) {
            log.error("Erro ao enviar dados iniciais para sessão {} - Erro: {}", session.getId(), e.getMessage(), e);
        }
    }

    public void notifyNewEvent(Evento evento) {

        log.info("Notificando novo evento para todas as sessões - Evento ID: {}", evento.getId());
        try {
            List<Dispositivo> dispositivos = dispositivoRepository.findAll();
            log.info("Total de dispositivos para notificação: {}", dispositivos.size());

            WebSocketMessageDTO message = new WebSocketMessageDTO();
            message.setDevices(dispositivos);

            NotificationDTO notification = new NotificationDTO(
                    evento.getId().toString(),
                    evento.getPhoneNumber(),
                    evento.getEventType().toString(),
                    evento.getDeviceId(),
                    evento.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    evento.getAdditionalData());

            message.setEvent(notification);
            log.debug("Notificação criada: {}", notification);

            String jsonMessage = objectMapper.writeValueAsString(message);
            log.debug("Mensagem serializada: {}", jsonMessage);

            TextMessage textMessage = new TextMessage(jsonMessage);
            int successCount = 0;
            int failCount = 0;

            for (WebSocketSession session : sessions.values()) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                        successCount++;
                    } else {
                        log.warn("Sessão {} está fechada, removendo da lista", session.getId());
                        sessions.remove(session.getId());
                    }
                } catch (IOException e) {
                    log.error("Erro ao enviar mensagem para sessão {} - Erro: {}", session.getId(), e.getMessage());
                    failCount++;
                }
            }

            log.info("Notificação enviada - Sucesso: {}, Falhas: {}, Total de sessões: {}",
                    successCount, failCount, sessions.size());

        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar mensagem para evento {} - Erro: {}", evento.getId(), e.getMessage(), e);
        }
    }
}

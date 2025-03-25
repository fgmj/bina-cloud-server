package com.bina.cloud.dto;

import com.bina.cloud.model.Dispositivo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WebSocketMessageDTO {
    private List<Dispositivo> devices;
    private NotificationDTO event;
}
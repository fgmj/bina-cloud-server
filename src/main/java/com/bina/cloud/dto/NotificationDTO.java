package com.bina.cloud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String eventId;
    private String description;
    private String eventType;
    private String deviceId;
    private String timestamp;
    private String additionalData;
}
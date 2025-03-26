package com.bina.cloud.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class DeviceStatsDTO {
    private String deviceId;
    private long totalCalls;
    private long answeredCalls;
    private long missedCalls;
    private long busyCalls;
    private double answerRate;
    private String lastActivity;
    private boolean isActive;
}
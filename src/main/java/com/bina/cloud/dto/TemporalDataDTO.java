package com.bina.cloud.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class TemporalDataDTO {
    private long timestamp;
    private int value;
} 
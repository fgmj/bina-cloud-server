package com.bina.cloud.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CallDTO {
    private String phoneNumber;
    private long timestamp;
    private String duration;
    private String status;
    private String device;
} 
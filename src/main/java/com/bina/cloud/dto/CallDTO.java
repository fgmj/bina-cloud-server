package com.bina.cloud.dto;

import com.bina.cloud.model.EventType;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CallDTO {
    private String phoneNumber;
    private long timestamp;
    private String duration;
    private EventType status;
    private String device;
}
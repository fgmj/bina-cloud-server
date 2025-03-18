package com.bina.cloud.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class DeviceStatsDTO {
    private List<Integer> values;
    private List<String> labels;
} 
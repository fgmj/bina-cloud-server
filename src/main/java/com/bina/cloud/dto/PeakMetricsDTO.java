package com.bina.cloud.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class PeakMetricsDTO {
    private String currentPeak;
    private String nextPeak;
    private String comparison;
} 
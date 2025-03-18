package com.bina.cloud.dto;

import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalCalls;
    private long answeredCalls;
    private long missedCalls;
    private double answerRate;

    private List<Integer> callsPerHour;
    private DeviceStatsDTO deviceStats;
    private List<List<Integer>> peakHours;
    private List<TemporalDataDTO> temporalData;
    private PeakMetricsDTO peakMetrics;
    private List<CallDTO> recentCalls;
}

package com.bina.cloud.dto;

import lombok.Data;
import lombok.Builder;

import java.util.List;
import java.util.Map;

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

@Data
@Builder
public class DeviceStatsDTO {
    private List<Integer> values;
    private List<String> labels;
}

@Data
@Builder
public class PeakMetricsDTO {
    private String currentPeak;
    private String nextPeak;
    private String comparison;
}

@Data
@Builder
public class TemporalDataDTO {
    private long timestamp;
    private int value;
}

@Data
@Builder
public class CallDTO {
    private String phoneNumber;
    private long timestamp;
    private String duration;
    private String status;
    private String device;
} 
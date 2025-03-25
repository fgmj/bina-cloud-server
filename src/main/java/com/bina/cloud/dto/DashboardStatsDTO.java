package com.bina.cloud.dto;

import com.bina.cloud.model.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalCalls;
    private long answeredCalls;
    private long missedCalls;
    private long busyCalls;
    private double answerRate;

    private List<Integer> callsPerHour;
    private DeviceStatsDTO deviceStats;
    private List<List<Integer>> peakHours;
    private List<TemporalDataDTO> temporalData;
    private PeakMetricsDTO peakMetrics;
    private List<CallDTO> recentCalls;
    private List<ActiveDeviceDTO> activeDevices;
}

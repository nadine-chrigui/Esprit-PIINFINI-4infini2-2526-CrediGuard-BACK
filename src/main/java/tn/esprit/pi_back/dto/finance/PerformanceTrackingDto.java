package tn.esprit.pi_back.dto.finance;

import lombok.Data;
import tn.esprit.pi_back.entities.PerformanceTracking;

@Data
public class PerformanceTrackingDto {
    private Long id;
    private String date;
    private double value;
    private double variation;

    public static PerformanceTrackingDto from(PerformanceTracking tracking) {
        PerformanceTrackingDto dto = new PerformanceTrackingDto();
        dto.id = tracking.getId();
        dto.date = tracking.getDate() != null ? tracking.getDate().toString() : null;
        dto.value = tracking.getValue();
        dto.variation = tracking.getVariation();
        return dto;
    }
}

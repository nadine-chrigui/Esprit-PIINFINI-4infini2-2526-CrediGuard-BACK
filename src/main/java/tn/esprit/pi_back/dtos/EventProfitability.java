package tn.esprit.pi_back.dtos;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EventProfitability {
    private Long eventId;
    private String eventName;
    private BigDecimal totalRevenue;
    private BigDecimal totalCosts;
    private BigDecimal profit;
    private BigDecimal profitMargin; // Pourcentage
    private Integer breakEvenAttendees;
    private Long currentRegistrations;
    private Boolean isProfitable;
    private LocalDate calculationDate;
}

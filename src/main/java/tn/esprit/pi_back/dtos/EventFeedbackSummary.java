package tn.esprit.pi_back.dtos;

import lombok.Builder;
import lombok.Data;
import tn.esprit.pi_back.entities.EventFeedback;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventFeedbackSummary {
    private Long eventId;
    private String eventName;
    private Integer totalFeedbacks;
    private Double averageOverallRating;
    private Double averageOrganizationRating;
    private Double averageContentRating;
    private Double averageVenueRating;
    private Double averageValueRating;
    private Double averageParticipationRating;
    private Double recommendationRate;
    private List<EventFeedback> recentFeedbacks;
    private LocalDateTime lastUpdated;
}

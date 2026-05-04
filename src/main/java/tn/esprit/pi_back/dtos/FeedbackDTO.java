package tn.esprit.pi_back.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackDTO {
    
    @NotNull
    @Min(1) @Max(5)
    private Integer overallRating;
    
    @Min(1) @Max(5)
    private Integer organizationRating;
    
    @Min(1) @Max(5)
    private Integer contentRating;
    
    @Min(1) @Max(5)
    private Integer venueRating;
    
    @Min(1) @Max(5)
    private Integer valueRating;
    
    @Min(1) @Max(5)
    private Integer participationRating;
    
    private String comment;
    
    private String suggestions;
    
    private Boolean wouldRecommend;
}

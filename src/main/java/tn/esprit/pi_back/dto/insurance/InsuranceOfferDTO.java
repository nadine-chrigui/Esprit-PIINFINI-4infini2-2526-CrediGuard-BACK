package tn.esprit.pi_back.dto.insurance;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class InsuranceOfferDTO {
    private Long id;
    private String name;
    private Double annualPremium;
    private String coverageDetails;
    private String guarantees;
    private String exclusions;
    private String type;
    private Double coverageAmount;
    private Double franchise;
    private Integer coverageRate;
    private String tags;
    private boolean active;
    private Long companyId;
    private String companyName;
    private Integer matchScore;
}

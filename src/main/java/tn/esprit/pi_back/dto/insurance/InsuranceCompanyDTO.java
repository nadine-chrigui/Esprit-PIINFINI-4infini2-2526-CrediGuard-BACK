package tn.esprit.pi_back.dto.insurance;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class InsuranceCompanyDTO {
    private Long id;
    private String name;
    private String registrationNumber;
    private String logoUrl;
    private String description;
    private List<String> categories;
    private Float reliabilityNote;
    private boolean active;
    private List<InsuranceOfferDTO> offers;
}

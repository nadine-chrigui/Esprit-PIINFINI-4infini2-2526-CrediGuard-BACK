package tn.esprit.pi_back.dto.insurance;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RecommendedOfferDTO extends InsuranceOfferDTO {
    private int adequacyScore;
}

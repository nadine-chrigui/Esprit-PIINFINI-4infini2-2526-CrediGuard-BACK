package tn.esprit.pi_back.dto.insurance;

import java.time.LocalDateTime;
import java.util.List;

public record InsuranceRecommendationDTO(
        Long id,
        Double riskScore,
        String recommendationText,
        List<InsuranceOfferDTO> suggestedOffers,
        LocalDateTime calculationDate
) {}

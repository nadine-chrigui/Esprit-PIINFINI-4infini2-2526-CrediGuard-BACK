package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.InsuranceOffer;
import tn.esprit.pi_back.entities.RiskScore;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.RiskScoreRepository;

@Service
@RequiredArgsConstructor
public class AdequacyServiceImpl implements AdequacyService {

    private final RiskScoreRepository riskScoreRepository;

    @Override
    public int calculateScore(InsuranceOffer offer, User client) {
        int score = 0;

        // 1. Sector Matching
        if (client.getSector() != null && offer.getTags() != null) {
            if (java.util.Arrays.stream(offer.getTags().split(",")).anyMatch(t -> t.trim().equalsIgnoreCase(client.getSector()))) {
                score += 30;
            }
        }

        // 2. Activity Type Matching
        if (client.getActivityType() != null && offer.getTags() != null) {
            if (java.util.Arrays.stream(offer.getTags().split(",")).anyMatch(t -> t.trim().equalsIgnoreCase(client.getActivityType()))) {
                score += 30;
            }
        }

        // 3. Region Matching (if any tag matches region)
        if (client.getRegion() != null && offer.getTags() != null) {
            if (java.util.Arrays.stream(offer.getTags().split(",")).anyMatch(t -> t.trim().equalsIgnoreCase(client.getRegion()))) {
                score += 20;
            }
        }

        // 4. Risk Score Alignment
        RiskScore riskScore = riskScoreRepository.findByUserId(client.getId()).orElse(null);
        if (riskScore != null) {
            // Logic: if risk is high (e.g. > 70), premium should be appropriate or coverage rate should be high
            // Let's say if risk is high, we value "COMPLETE" coverage tags more
            if (riskScore.getGlobalScore() > 70 && java.util.Arrays.stream(offer.getTags().split(",")).anyMatch(t -> t.trim().equalsIgnoreCase("COMPLET"))) {
                score += 20;
            } else if (riskScore.getGlobalScore() < 30 && java.util.Arrays.stream(offer.getTags().split(",")).anyMatch(t -> t.trim().equalsIgnoreCase("ECONOMIQUE"))) {
                score += 20;
            }
        }

        return Math.min(score, 100);
    }
}

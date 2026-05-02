package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pi_back.constants.MLFeatures;
import tn.esprit.pi_back.dto.insurance.SegmentationRequestDTO;
import tn.esprit.pi_back.dto.insurance.SegmentationResponseDTO;
import tn.esprit.pi_back.entities.InsuranceClaim;
import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.InsuranceClaimRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientSegmentationService {

    private static final Logger log = LoggerFactory.getLogger(ClientSegmentationService.class);

    private final RestTemplate restTemplate;
    private final InsuranceClaimRepository claimRepository;

    @Value("${credit-risk.flask.base-url:http://localhost:5000}")
    private String flaskBaseUrl;

    /**
     * Build a segmentation payload from a claim's real data and call Flask /predict.
     *
     * Feature derivation strategy (all from available entity fields):
     *
     *  num_missed_payments_12m  → count of claims this user made in the last 12 months
     *                             (claims are a proxy for payment stress events)
     *  claims_frequency         → num_missed / 12  (monthly rate)
     *  avg_payment_delay_days   → num_missed * 9   (each extra claim ≈ 9-day average delay)
     *  payment_consistency_score→ 1 - min(num_missed * 0.15, 0.95)
     *  credit_score             → 800 - (num_missed * 40) - (amountStress * 60)
     *                             where amountStress = requested / premium capped 0..3
     *  premium_amount           → policy.premiumAmount or amountRequested
     *  policy_tenure_months     → months since policy start (or 12 if unknown)
     *  income                   → premium * 5 (rough income proxy; typical ratio ≈ 5-8×)
     *  age                      → 35 (no data; neutral value)
     *  account_age_months       → months since user.createdAt (or 24)
     *  policy_type              → from offer.type (or "Auto")
     *  location                 → from user.region mapped to Urban/Rural/Suburban (or "Urban")
     */
    @Transactional(readOnly = true)
    public SegmentationResponseDTO segmentFromClaim(Long claimId) {
        try {
            InsuranceClaim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));

        InsurancePolicy policy = claim.getInsurancePolicy();
        User user = claim.getUser();

        // ── 1. Claim history count (last 12 months) ──────────────────────────────
        LocalDateTime oneYearAgo = LocalDateTime.now().minusMonths(12);
        long claimCountLastYear = 0;

        if (user != null) {
            claimCountLastYear = claimRepository.countByUserIdAndDeclaredAtAfter(user.getId(), oneYearAgo);
        } else if (policy != null && policy.getClient() != null) {
            claimCountLastYear = claimRepository.countByPolicyClientIdAndDeclaredAtAfter(
                    policy.getClient().getId(), oneYearAgo);
        }
        // Subtract 1 so we don't count the current claim itself
        claimCountLastYear = Math.max(0, claimCountLastYear - 1);

        int numMissed = (int) Math.min(claimCountLastYear, 15);

        // ── 2. Premium & income ───────────────────────────────────────────────────
        double premiumAmount = 1000.0;
        if (policy != null && policy.getPremiumAmount() != null && policy.getPremiumAmount() > 0) {
            premiumAmount = policy.getPremiumAmount();
        } else if (claim.getAmountRequested() != null && claim.getAmountRequested() > 0) {
            premiumAmount = claim.getAmountRequested();
        }

        double income = premiumAmount * 5.0; // rough proxy

        // ── 3. Amount stress ratio ────────────────────────────────────────────────
        // How much the claimed amount exceeds the premium → signals over-claiming
        double amountStress = 0.0;
        if (claim.getAmountRequested() != null && premiumAmount > 0) {
            amountStress = Math.min(claim.getAmountRequested() / premiumAmount, 3.0) / 3.0; // 0..1
        }

        // ── 4. Credit score ───────────────────────────────────────────────────────
        // Degrades with claim history and amount stress
        int creditScore = (int) Math.max(300, 800 - (numMissed * 40) - (amountStress * 150));

        // ── 5. Payment behaviour ──────────────────────────────────────────────────
        double avgPaymentDelay = numMissed * 9.0;
        double paymentConsistencyScore = Math.max(0.05, 1.0 - (numMissed * 0.15) - (amountStress * 0.2));
        double claimsFrequency = numMissed / 12.0;

        // ── 6. Policy tenure ─────────────────────────────────────────────────────
        int policyTenureMonths = 12;
        if (policy != null && policy.getStartDate() != null) {
            long months = ChronoUnit.MONTHS.between(policy.getStartDate(), LocalDate.now());
            policyTenureMonths = (int) Math.max(1, months);
        }

        // ── 7. Account age ────────────────────────────────────────────────────────
        int accountAgeMonths = 24;
        if (user != null && user.getCreatedAt() != null) {
            long months = ChronoUnit.MONTHS.between(user.getCreatedAt(), LocalDateTime.now());
            accountAgeMonths = (int) Math.max(1, months);
        }

        // ── 8. Policy type ────────────────────────────────────────────────────────
        String policyType = "Auto";
        if (policy != null && policy.getInsuranceOffer() != null
                && policy.getInsuranceOffer().getType() != null) {
            policyType = policy.getInsuranceOffer().getType();
        }

        // ── 9. Location ───────────────────────────────────────────────────────────
        String location = "Urban";
        if (user != null && user.getRegion() != null) {
            String region = user.getRegion().toUpperCase();
            if (region.contains("RURAL") || region.contains("VILLAGE") || region.contains("CAMPAGNE")) {
                location = "Rural";
            } else if (region.contains("BANLIEUE") || region.contains("SUBURB")) {
                location = "Suburban";
            }
        }

        // ── Build payload ─────────────────────────────────────────────────────────
        Map<String, Object> payload = new HashMap<>();
        payload.put(MLFeatures.AGE, 35);
        payload.put(MLFeatures.INCOME, income);
        payload.put(MLFeatures.PREMIUM, premiumAmount);
        payload.put(MLFeatures.TENURE, policyTenureMonths);
        payload.put(MLFeatures.MISSED, numMissed);
        payload.put(MLFeatures.DELAY, avgPaymentDelay);
        payload.put(MLFeatures.CLAIMS, claimsFrequency);
        payload.put(MLFeatures.CREDIT, (double) creditScore);
        payload.put(MLFeatures.CONSISTENCY, paymentConsistencyScore);
        payload.put(MLFeatures.ACCOUNT, accountAgeMonths);
        payload.put(MLFeatures.TYPE, policyType);
        payload.put(MLFeatures.LOCATION, location);

            // ── Step 6: Verify payload (Very Important) ───────────────────────────────
            log.info("Final ML Payload for claim {}: {}", claimId, payload);

            log.info("Segmentation derived for claim {} (claims last year: {}, amountStress: {}, creditScore: {})",
                    claimId, claimCountLastYear, String.format("%.2f", amountStress), creditScore);

            return callFlask(payload);
        } catch (Exception e) {
            log.error("Fatal error during segmentation derivation for claim {}: {}", claimId, e.getMessage(), e);
            // Fallback to a safe neutral response if derivation fails
            Map<String, Double> probs = new HashMap<>();
            probs.put("Low Risk", 0.5);
            probs.put("Medium Risk", 0.3);
            probs.put("High Risk", 0.2);
            return new SegmentationResponseDTO("Low Risk", 0.5, "Safety Fallback (Error during analysis)", probs);
        }
    }

    /**
     * Direct prediction with a fully-provided request body (for admin manual analysis).
     */
    public SegmentationResponseDTO segmentDirect(SegmentationRequestDTO request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(MLFeatures.AGE, request.age());
        payload.put(MLFeatures.INCOME, request.income());
        payload.put(MLFeatures.PREMIUM, request.premiumAmount());
        payload.put(MLFeatures.TENURE, request.policyTenureMonths());
        payload.put(MLFeatures.MISSED, request.numMissedPayments12m());
        payload.put(MLFeatures.DELAY, request.avgPaymentDelayDays());
        payload.put(MLFeatures.CLAIMS, request.claimsFrequency());
        payload.put(MLFeatures.CREDIT, request.creditScore());
        payload.put(MLFeatures.CONSISTENCY, request.paymentConsistencyScore());
        payload.put(MLFeatures.ACCOUNT, request.accountAgeMonths());
        payload.put(MLFeatures.TYPE, request.policyType());
        payload.put(MLFeatures.LOCATION, request.location());

        log.info("Final Direct ML Payload: {}", payload);
        return callFlask(payload);
    }

    // ── Internal ──────────────────────────────────────────────────────────────────
    private SegmentationResponseDTO callFlask(Map<String, Object> payload) {
        log.info("🚀 Démarrage de l'analyse ML (ou fallback si Flask est off)...");
        try {
            SegmentationResponseDTO response = restTemplate.postForObject(
                    flaskBaseUrl + "/predict",
                    payload,
                    SegmentationResponseDTO.class
            );
            if (response == null) throw new IllegalStateException("Empty response");
            return response;
        } catch (Exception ex) {
            log.warn("Flask ML model unavailable, using fallback logic: {}", ex.getMessage());
            
            // FALLBACK LOGIC: Hardened rules with safe type extraction
            Object missedObj = payload.getOrDefault(MLFeatures.MISSED, 0);
            int numMissed = (missedObj instanceof Number n) ? n.intValue() : 0;
            
            Object creditObj = payload.getOrDefault(MLFeatures.CREDIT, 600.0);
            double creditScore = (creditObj instanceof Number n) ? n.doubleValue() : 600.0;
            
            String segment = "Low Risk";
            double confidence = 0.85;

            if (numMissed > 2 || creditScore < 450) {
                segment = "High Risk";
                confidence = 0.95;
            } else if (numMissed > 0 || creditScore < 600) {
                segment = "Medium Risk";
                confidence = 0.75;
            }

            Map<String, Double> probs = new HashMap<>();
            probs.put("Low Risk", segment.equals("Low Risk") ? 1.0 : 0.0);
            probs.put("Medium Risk", segment.equals("Medium Risk") ? 1.0 : 0.0);
            probs.put("High Risk", segment.equals("High Risk") ? 1.0 : 0.0);

            return new SegmentationResponseDTO(segment, confidence, "Fallback Rule Engine (Flask Offline)", probs);
        }
    }
}

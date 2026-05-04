package tn.esprit.pi_back.dto.insurance;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SegmentationRequestDTO(
        int age,
        double income,
        @JsonProperty("premium_amount") double premiumAmount,
        @JsonProperty("policy_tenure_months") int policyTenureMonths,
        @JsonProperty("num_missed_payments_12m") int numMissedPayments12m,
        @JsonProperty("avg_payment_delay_days") double avgPaymentDelayDays,
        @JsonProperty("claims_frequency") double claimsFrequency,
        @JsonProperty("credit_score") double creditScore,
        @JsonProperty("payment_consistency_score") double paymentConsistencyScore,
        @JsonProperty("account_age_months") int accountAgeMonths,
        @JsonProperty("policy_type") String policyType,
        String location
) {}

package tn.esprit.pi_back.dto.InvestmentPayment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tn.esprit.pi_back.entities.enums.PaymentScheduleFrequency;

public record InvestmentPaymentIntentRequest(
        @NotNull @Positive Double amount,
        @NotNull Long investorId,
        @NotNull Long projectId,
        @NotNull @Min(1) Integer durationYears,
        @NotNull PaymentScheduleFrequency scheduleFrequency
) {}

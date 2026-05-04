package tn.esprit.pi_back.dto.InvestmentPayment;

import tn.esprit.pi_back.entities.enums.InvestmentPaymentStatus;
import tn.esprit.pi_back.entities.enums.PaymentScheduleFrequency;

import java.time.LocalDateTime;

public record InvestmentPaymentResponse(
        Long id,
        Long investorId,
        Long projectId,
        Long investmentId,
        Double amount,
        String currency,
        Integer durationYears,
        PaymentScheduleFrequency scheduleFrequency,
        Double interestRateSnapshot,
        Double expectedReturnSnapshot,
        String stripePaymentIntentId,
        InvestmentPaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt
) {}

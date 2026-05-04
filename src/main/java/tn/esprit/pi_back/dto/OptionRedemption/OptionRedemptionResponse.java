package tn.esprit.pi_back.dto.OptionRedemption;

import java.time.LocalDate;

public record OptionRedemptionResponse(
        Long redemptionId,
        Integer redeemedQuantity,
        LocalDate redemptionDate,
        Double finalPrice,
        Double commissionAmount,
        Long subscriptionId,
        Long orderId
) {}

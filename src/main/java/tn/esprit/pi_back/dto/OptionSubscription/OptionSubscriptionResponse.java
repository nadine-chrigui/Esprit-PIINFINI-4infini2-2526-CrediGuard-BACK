package tn.esprit.pi_back.dto.OptionSubscription;

import java.time.LocalDate;

public record OptionSubscriptionResponse(
        Long subscriptionId,
        Integer reservedQuantity,
        LocalDate subscriptionDate,
        Double amountPaid,
        String status,
        Long userId,
        Long purchaseOptionId
) {}

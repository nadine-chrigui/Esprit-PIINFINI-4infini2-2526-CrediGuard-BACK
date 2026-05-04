package tn.esprit.pi_back.dto.PurchaseOption;

import java.time.LocalDate;

public record PurchaseOptionResponse(
        Long optionId,
        Double fixedPrice,
        Integer maxQuantity,
        Integer soldQuantity,
        Integer remainingQuantity,
        Double commissionRate,
        LocalDate expirationDate,
        String status,
        Long projectId
) {}

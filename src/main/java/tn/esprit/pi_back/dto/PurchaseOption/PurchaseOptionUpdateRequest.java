package tn.esprit.pi_back.dto.PurchaseOption;

import tn.esprit.pi_back.entities.PurchaseOption;

import java.time.LocalDate;

public record PurchaseOptionUpdateRequest(
        Double fixedPrice,
        Integer maxQuantity,
        Double commissionRate,
        LocalDate expirationDate,
        PurchaseOption.OptionStatus status
) {}

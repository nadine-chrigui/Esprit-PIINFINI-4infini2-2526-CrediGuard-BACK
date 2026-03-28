package tn.esprit.pi_back.dto.promocode;



public record PromoCodeValidateResponse(
        boolean valid,
        String message,
        Double discountApplied,
        Double finalAmount,
        Long promoCodeId
) {}
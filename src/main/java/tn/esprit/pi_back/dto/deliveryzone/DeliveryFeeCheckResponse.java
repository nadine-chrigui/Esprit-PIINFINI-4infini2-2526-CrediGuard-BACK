package tn.esprit.pi_back.dto.deliveryzone;

public record DeliveryFeeCheckResponse(
        String areaLabel,
        String areaColor,
        Double deliveryFee,
        Integer estimatedDelayDays,
        String message
) {}

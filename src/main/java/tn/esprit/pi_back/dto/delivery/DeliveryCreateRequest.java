package tn.esprit.pi_back.dto.delivery;

import jakarta.validation.constraints.NotNull;
import tn.esprit.pi_back.entities.enums.DeliverySlot;
import tn.esprit.pi_back.entities.enums.DeliveryType;

import java.time.LocalDateTime;

public record DeliveryCreateRequest(
        @NotNull Long orderId,
        @NotNull Long addressId,
        @NotNull DeliveryType deliveryType,
        DeliverySlot deliverySlot,
        Double deliveryFee,
        LocalDateTime scheduledAt,
        String trackingNumber,
        String carrier
) {}
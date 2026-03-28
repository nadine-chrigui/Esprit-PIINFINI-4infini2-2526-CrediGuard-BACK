package tn.esprit.pi_back.dto.delivery;

import tn.esprit.pi_back.entities.enums.DeliverySlot;
import tn.esprit.pi_back.entities.enums.DeliveryStatus;
import tn.esprit.pi_back.entities.enums.DeliveryType;

import java.time.LocalDateTime;

public record DeliveryUpdateRequest(
        DeliveryType deliveryType,
        DeliveryStatus deliveryStatus,
        DeliverySlot deliverySlot,
        Double deliveryFee,
        LocalDateTime scheduledAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        String trackingNumber,
        String carrier,
        Long addressId
) {}
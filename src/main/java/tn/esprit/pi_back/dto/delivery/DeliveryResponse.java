package tn.esprit.pi_back.dto.delivery;

import tn.esprit.pi_back.entities.enums.DeliverySlot;
import tn.esprit.pi_back.entities.enums.DeliveryStatus;
import tn.esprit.pi_back.entities.enums.DeliveryType;
import tn.esprit.pi_back.entities.enums.DeliveryZoneRisk;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        Long orderId,
        DeliveryType deliveryType,
        DeliveryStatus deliveryStatus,
        DeliverySlot deliverySlot,
        Double deliveryFee,
        LocalDateTime scheduledAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        String trackingNumber,
        String carrier,
        Long deliveryZoneId,
        String deliveryZoneName,
        DeliveryZoneRisk zoneRiskLevel,
        Integer extraDelayDays,
        Boolean requiresAdminApproval,
        DeliveryAddressResponse address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

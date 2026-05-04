package tn.esprit.pi_back.dto.deliveryzone;

import tn.esprit.pi_back.entities.enums.DeliveryZoneRisk;

public record DeliveryZoneCheckResponse(
        boolean matched,
        Long zoneId,
        String zoneName,
        DeliveryZoneRisk riskLevel,
        String riskLabel,
        String riskColor,
        Double feeAdjustment,
        Integer extraDelayDays,
        Boolean requiresAdminApproval,
        String message
) {}

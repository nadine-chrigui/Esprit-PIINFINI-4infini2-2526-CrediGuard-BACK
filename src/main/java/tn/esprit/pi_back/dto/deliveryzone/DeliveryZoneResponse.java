package tn.esprit.pi_back.dto.deliveryzone;

import tn.esprit.pi_back.entities.enums.DeliveryZoneRisk;

import java.time.LocalDateTime;

public record DeliveryZoneResponse(
        Long id,
        String name,
        String governorate,
        String delegation,
        String locality,
        DeliveryZoneRisk riskLevel,
        String riskLabel,
        String riskColor,
        Double feeAdjustment,
        Integer extraDelayDays,
        Boolean requiresAdminApproval,
        Boolean active,
        String reason,
        String geoJsonPolygon,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

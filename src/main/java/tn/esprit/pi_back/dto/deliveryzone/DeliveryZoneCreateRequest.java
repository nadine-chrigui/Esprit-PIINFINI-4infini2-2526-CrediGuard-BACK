package tn.esprit.pi_back.dto.deliveryzone;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.DeliveryZoneRisk;

public record DeliveryZoneCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 100) String governorate,
        @Size(max = 100) String delegation,
        @Size(max = 120) String locality,
        @NotNull DeliveryZoneRisk riskLevel,
        @PositiveOrZero Double feeAdjustment,
        @PositiveOrZero Integer extraDelayDays,
        Boolean requiresAdminApproval,
        Boolean active,
        @Size(max = 255) String reason,
        @NotBlank String geoJsonPolygon
) {}

package tn.esprit.pi_back.dto.deliveryzone;

import jakarta.validation.constraints.*;

public record DeliveryZoneCheckRequest(
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {}

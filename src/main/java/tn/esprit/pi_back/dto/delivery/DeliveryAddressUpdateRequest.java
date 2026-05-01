package tn.esprit.pi_back.dto.delivery;

import jakarta.validation.constraints.*;

public record DeliveryAddressUpdateRequest(
        @Size(min = 2, max = 120) String fullName,
        @Pattern(regexp = "^[0-9]{8}$") String phone,
        @Size(min = 2, max = 100) String city,
        @Size(max = 100) String governorate,
        @Size(max = 100) String delegation,
        @Size(max = 120) String locality,
        @Size(min = 5, max = 255) String addressLine,
        @Size(max = 255) String additionalInfo,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {}

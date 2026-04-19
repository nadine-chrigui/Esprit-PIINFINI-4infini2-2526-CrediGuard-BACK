package tn.esprit.pi_back.dto.delivery;

import jakarta.validation.constraints.*;

public record DeliveryAddressUpdateRequest(
        @Size(min = 2, max = 120) String fullName,
        @Pattern(regexp = "^[0-9]{8}$") String phone,
        @Size(min = 2, max = 100) String city,
        @Size(min = 5, max = 255) String addressLine,
        @Size(max = 255) String additionalInfo
) {}
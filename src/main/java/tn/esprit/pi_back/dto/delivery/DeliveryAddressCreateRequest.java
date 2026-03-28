package tn.esprit.pi_back.dto.delivery;

import jakarta.validation.constraints.*;

public record DeliveryAddressCreateRequest(
        @NotBlank @Size(min = 2, max = 120) String fullName,
        @NotBlank @Pattern(regexp = "^[0-9]{8}$") String phone,
        @NotBlank @Size(min = 2, max = 100) String city,
        @NotBlank @Size(min = 5, max = 255) String addressLine,
        @Size(max = 255) String additionalInfo
) {}
package tn.esprit.pi_back.dto.deliveryzone;

import jakarta.validation.constraints.Size;

public record DeliveryFeeCheckRequest(
        @Size(max = 100) String governorate,
        @Size(max = 100) String city,
        @Size(max = 100) String delegation,
        @Size(max = 120) String locality,
        @Size(max = 255) String addressLine
) {}

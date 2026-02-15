package tn.esprit.pi_back.dto.delivery;

public record DeliveryAddressResponse(
        Long id,
        String fullName,
        String phone,
        String city,
        String addressLine,
        String additionalInfo
) {}
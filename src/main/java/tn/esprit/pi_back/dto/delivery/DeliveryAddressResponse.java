package tn.esprit.pi_back.dto.delivery;

public record DeliveryAddressResponse(
        Long id,
        String fullName,
        String phone,
        String city,
        String governorate,
        String delegation,
        String locality,
        String addressLine,
        String additionalInfo,
        Double latitude,
        Double longitude
) {}

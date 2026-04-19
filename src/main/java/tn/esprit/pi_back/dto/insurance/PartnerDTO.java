package tn.esprit.pi_back.dto.insurance;

import tn.esprit.pi_back.entities.enums.PartnerType;

public record PartnerDTO(
        Long id,
        String fullName,
        String email,
        PartnerType partnerType
) {}
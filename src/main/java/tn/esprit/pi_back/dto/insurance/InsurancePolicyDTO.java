package tn.esprit.pi_back.dto.insurance;

import java.time.LocalDate;

public record InsurancePolicyDTO(
        Long id,
        String policyNumber,
        LocalDate startDate,
        LocalDate endDate,
        InsuranceCompanyMiniDTO company,
        ClientDTO client
) {}
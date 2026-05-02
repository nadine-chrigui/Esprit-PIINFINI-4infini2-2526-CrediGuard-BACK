package tn.esprit.pi_back.dto.insurance;

import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.dto.insurance.UserMapper;
import tn.esprit.pi_back.dto.insurance.ClientDTO;
import tn.esprit.pi_back.dto.insurance.InsuranceOfferDTO;
import tn.esprit.pi_back.dto.insurance.InsuranceMapper;

public class InsurancePolicyMapper {

    public static InsurancePolicyDTO toDTO(InsurancePolicy p) {
        if (p == null) return null;

        // ── Company (Null-safe mapping with fallback to offer) ───
        InsuranceCompanyMiniDTO companyDto = null;
        if (p.getInsuranceCompany() != null) {
            companyDto = new InsuranceCompanyMiniDTO(
                    p.getInsuranceCompany().getId(),
                    p.getInsuranceCompany().getName()
            );
        } else if (p.getInsuranceOffer() != null && p.getInsuranceOffer().getInsuranceCompany() != null) {
            // Fallback : Utiliser l'assureur de l'offre
            companyDto = new InsuranceCompanyMiniDTO(
                    p.getInsuranceOffer().getInsuranceCompany().getId(),
                    p.getInsuranceOffer().getInsuranceCompany().getName()
            );
        }

        // ── Client (Null-safe) ───────────────────────────────────
        ClientDTO clientDto = null;
        if (p.getClient() != null) {
            try {
                clientDto = UserMapper.toClientDTO(p.getClient());
            } catch (Exception e) {
                // Si UserMapper échoue, on crée un DTO minimal pour éviter le crash 500
                clientDto = new ClientDTO();
                clientDto.setId(p.getClient().getId());
            }
        }

        // ── Offer (Null-safe mapping) ────────────────────────────
        InsuranceOfferDTO offerDto = null;
        if (p.getInsuranceOffer() != null) {
            offerDto = InsuranceMapper.toOfferDTO(p.getInsuranceOffer());
        }

        // ── Status (Enum to String, null-safe) ───────────────────
        String statusStr = p.getStatus() != null ? p.getStatus().name() : "PENDING";

        return new InsurancePolicyDTO(
                p.getId(),
                p.getPolicyNumber(),
                p.getStartDate(),
                p.getEndDate(),
                companyDto,
                clientDto,
                statusStr,
                (p.getPremiumAmount() != null) ? p.getPremiumAmount() : (p.getInsuranceOffer() != null ? p.getInsuranceOffer().getAnnualPremium() : 0.0),
                offerDto
        );
    }
}

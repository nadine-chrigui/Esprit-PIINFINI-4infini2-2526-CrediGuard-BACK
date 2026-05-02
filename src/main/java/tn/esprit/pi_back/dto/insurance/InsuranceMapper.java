package tn.esprit.pi_back.dto.insurance;

import tn.esprit.pi_back.entities.InsuranceCompany;
import tn.esprit.pi_back.entities.InsuranceOffer;
import tn.esprit.pi_back.entities.RiskScore;
import tn.esprit.pi_back.entities.InsuranceRecommendation;
import java.util.stream.Collectors;

public class InsuranceMapper {

    public static InsuranceRecommendationDTO toRecommendationDTO(InsuranceRecommendation r) {
        if (r == null) return null;
        return new InsuranceRecommendationDTO(
                r.getId(),
                r.getRiskScore(),
                r.getRecommendationText(),
                r.getSuggestedOffers() != null ? r.getSuggestedOffers().stream().map(InsuranceMapper::toOfferDTO).toList() : null,
                r.getCalculationDate()
        );
    }

    public static RiskScoreDTO toRiskScoreDTO(RiskScore rs) {
        if (rs == null) return null;
        return new RiskScoreDTO(
                rs.getId(),
                rs.getGlobalScore(),
                rs.getSectorScore(),
                rs.getRegionScore(),
                rs.getHistoryScore(),
                rs.getFactorDetails(),
                rs.getComputedAt()
        );
    }

    public static InsuranceCompanyDTO toCompanyDTO(InsuranceCompany company) {
        if (company == null) return null;
        InsuranceCompanyDTO dto = new InsuranceCompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setRegistrationNumber(company.getRegistrationNumber());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setDescription(company.getDescription());
        dto.setCategories(company.getCategories() != null ? company.getCategories() : new java.util.ArrayList<>());
        dto.setReliabilityNote(company.getReliabilityNote());
        dto.setActive(company.isActive());
        dto.setOffers(new java.util.ArrayList<>()); // Default to empty list
        return dto;
    }

    public static InsuranceOfferDTO toOfferDTO(InsuranceOffer offer) {
        if (offer == null) return null;
        InsuranceOfferDTO dto = new InsuranceOfferDTO();
        dto.setId(offer.getId());
        dto.setName(offer.getName());
        dto.setAnnualPremium(offer.getAnnualPremium());
        dto.setCoverageDetails(offer.getCoverageDetails());
        dto.setGuarantees(offer.getGuarantees());
        dto.setExclusions(offer.getExclusions());
        dto.setType(offer.getType());
        dto.setCoverageAmount(offer.getCoverageAmount());
        dto.setFranchise(offer.getFranchise());
        dto.setCoverageRate(offer.getCoverageRate());
        dto.setTags(offer.getTags());
        dto.setActive(offer.isActive());
        if (offer.getInsuranceCompany() != null) {
            dto.setCompanyId(offer.getInsuranceCompany().getId());
            dto.setCompanyName(offer.getInsuranceCompany().getName());
        }
        
        // CALCUL DYNAMIQUE DU SCORE (Simulation)
        int base = 70 + (int)(offer.getId() % 15); // Varie selon l'ID
        if (offer.getAnnualPremium() != null && offer.getAnnualPremium() > 1000) base += 5;
        if (offer.getTags() != null && offer.getTags().contains("PREMIUM")) base += 7;
        dto.setMatchScore(Math.min(base, 98));
        
        return dto;
    }
}

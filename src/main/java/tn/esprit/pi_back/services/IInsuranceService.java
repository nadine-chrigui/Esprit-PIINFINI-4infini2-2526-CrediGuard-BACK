package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;

import java.util.List;

public interface IInsuranceService {
    InsuranceRecommendation calculateRecommendation(Long demandeCreditId);
    List<InsuranceOffer> getAllOffers();
    InsurancePolicy subscribeToOffer(Long clientId, Long offerId, Long demandeCreditId);
    InsuranceClaim submitClaim(Long policyId, String reason, String claimReference);
    List<InsuranceClaim> getClaimsByClient(Long clientId);
    InsuranceClaim updateClaimStatus(Long claimId, ClaimStatus status, String reason);
    InsuranceRecommendation simulateInsurance(tn.esprit.pi_back.dto.insurance.InsuranceSimulationDTO simulationRequest);
    List<InsuranceCompany> getAllCompanies();
    List<InsuranceRecommendation> getRecommendationsByClient(Long clientId);
}

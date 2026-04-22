package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import tn.esprit.pi_back.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import tn.esprit.pi_back.dto.insurance.InsuranceSimulationDTO;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements IInsuranceService {

    private final InsuranceOfferRepository offerRepository;
    private final InsuranceRecommendationRepository recommendationRepository;
    private final InsurancePolicyRepository policyRepository;
    private final InsuranceClaimRepository claimRepository;
    private final DemandeCreditRepository demandeCreditRepository;
    private final UserRepository userRepository;
    private final InsuranceCompanyRepository companyRepository;

    @Override
    public InsuranceRecommendation calculateRecommendation(Long demandeCreditId) {
        DemandeCredit demande = demandeCreditRepository.findById(demandeCreditId)
                .orElseThrow(() -> new RuntimeException("Demande de crédit non trouvée"));

        // Decision Engine logic
        double riskScore = evaluateRisk(demande);
        
        InsuranceRecommendation recommendation = recommendationRepository.findByDemandeCreditId(demandeCreditId)
                .orElse(new InsuranceRecommendation());
        
        recommendation.setDemandeCredit(demande);
        recommendation.setRiskScore(riskScore);
        
        String recText = "Analysis for loan of " + demande.getMontantDemande() + " DT: ";
        if (riskScore < 30) {
            recText += "Low risk profile. High coverage offers recommended.";
        } else if (riskScore < 70) {
            recText += "Moderate risk profile. Standard offers are suitable.";
        } else {
            recText += "High risk profile. Basic coverage with specific guarantees recommended.";
        }
        recommendation.setRecommendationText(recText);

        // Filter offers based on risk
        List<InsuranceOffer> allOffers = offerRepository.findAll();
        List<InsuranceOffer> suggested = allOffers.stream()
                .filter(o -> {
                    if (riskScore < 30) return o.getAnnualPremium() > 400;
                    if (riskScore < 70) return o.getAnnualPremium() >= 150 && o.getAnnualPremium() <= 400;
                    return o.getAnnualPremium() < 150;
                })
                .limit(3)
                .collect(Collectors.toList());
        
        recommendation.setSuggestedOffers(suggested);
        recommendation.setCalculationDate(LocalDateTime.now());
        
        return recommendationRepository.save(recommendation);
    }

    private double evaluateRisk(DemandeCredit demande) {
        double score = 0;
        if (demande.getMontantDemande() > 50000) score += 40;
        else if (demande.getMontantDemande() > 20000) score += 20;
        else score += 10;

        if (demande.getDureeMois() > 60) score += 30;
        else if (demande.getDureeMois() > 36) score += 15;
        else score += 5;
        
        return score;
    }

    @Override
    public List<InsuranceOffer> getAllOffers() {
        return offerRepository.findAll();
    }

    @Override
    public InsurancePolicy subscribeToOffer(Long clientId, Long offerId, Long demandeCreditId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        InsuranceOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        InsurancePolicy policy = new InsurancePolicy();
        policy.setClient(client);
        policy.setInsuranceOffer(offer);
        policy.setInsuranceCompany(offer.getInsuranceCompany());
        policy.setPolicyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1));

        return policyRepository.save(policy);
    }

    @Override
    public InsuranceClaim submitClaim(Long policyId, String reason, String claimReference) {
        InsurancePolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Police d'assurance non trouvée"));

        InsuranceClaim claim = new InsuranceClaim();
        claim.setInsurancePolicy(policy);
        claim.setReason(reason);
        claim.setClaimReference(claimReference != null ? claimReference : "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        claim.setStatus(ClaimStatus.PENDING);
        claim.setCreatedAt(LocalDateTime.now());
        
        return claimRepository.save(claim);
    }

    @Override
    public List<InsuranceClaim> getClaimsByClient(Long clientId) {
        List<InsurancePolicy> policies = policyRepository.findByClientId(clientId);
        List<Long> policyIds = policies.stream().map(InsurancePolicy::getId).collect(Collectors.toList());
        
        return claimRepository.findAll().stream()
                .filter(c -> policyIds.contains(c.getInsurancePolicy().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public InsuranceClaim updateClaimStatus(Long claimId, ClaimStatus status, String reason) {
        InsuranceClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Sinistre non trouvé"));
        
        claim.setStatus(status);
        if (reason != null) claim.setReason(reason);
        claim.setDecidedAt(LocalDateTime.now());
        
        return claimRepository.save(claim);
    }

    @Override
    public InsuranceRecommendation simulateInsurance(InsuranceSimulationDTO req) {
        // Create a transient DemandeCredit for the logic
        DemandeCredit pseudoDemande = new DemandeCredit();
        pseudoDemande.setMontantDemande(req.getAmount());
        pseudoDemande.setDureeMois(req.getDurationInMonths());
        pseudoDemande.setTypeCredit(req.getLoanType());

        double riskScore = evaluateRisk(pseudoDemande);
        
        InsuranceRecommendation recommendation = new InsuranceRecommendation();
        recommendation.setRiskScore(riskScore);
        
        String recText = "Simulation analysis for " + req.getAmount() + " DT: ";
        if (riskScore < 30) {
            recText += "Low risk profile. High coverage offers recommended.";
        } else if (riskScore < 70) {
            recText += "Moderate risk profile. Standard offers are suitable.";
        } else {
            recText += "High risk profile. Basic coverage with specific guarantees recommended.";
        }
        recommendation.setRecommendationText(recText);

        // Filter offers based on risk
        List<InsuranceOffer> allOffers = offerRepository.findAll();
        List<InsuranceOffer> suggested = allOffers.stream()
                .filter(o -> {
                    if (riskScore < 30) return o.getAnnualPremium() > 400;
                    if (riskScore < 70) return o.getAnnualPremium() >= 150 && o.getAnnualPremium() <= 400;
                    return o.getAnnualPremium() < 150;
                })
                .limit(3)
                .collect(Collectors.toList());
        
        recommendation.setSuggestedOffers(suggested);
        recommendation.setCalculationDate(LocalDateTime.now());
        
        return recommendation;
    }

    @Override
    public List<InsuranceCompany> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public List<InsuranceRecommendation> getRecommendationsByClient(Long clientId) {
        return recommendationRepository.findByDemandeCreditClientId(clientId);
    }
}

package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import tn.esprit.pi_back.services.IInsuranceService;
import tn.esprit.pi_back.dto.insurance.*;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.services.InsuranceRagService;

import java.util.List;

@RestController
@RequestMapping("/insurance")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class InsuranceController {

    private final IInsuranceService insuranceService;
    private final UserRepository userRepository;
    private final InsuranceRagService insuranceRagService;

    @PostMapping("/simulate")
    public ResponseEntity<InsuranceRecommendationDTO> simulate(@RequestBody InsuranceSimulationDTO simulationRequest) {
        InsuranceRecommendation recommendation = insuranceService.simulateInsurance(simulationRequest);
        return ResponseEntity.ok(InsuranceMapper.toRecommendationDTO(recommendation));
    }

    @GetMapping("/recommendations/client/{clientId}")
    public ResponseEntity<List<InsuranceRecommendationDTO>> getRecommendationsByClient(@PathVariable Long clientId) {
        List<InsuranceRecommendationDTO> dtos = insuranceService.getRecommendationsByClient(clientId)
                .stream()
                .map(InsuranceMapper::toRecommendationDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/recommendation/{demandeId}")
    public ResponseEntity<InsuranceRecommendationDTO> getRecommendation(@PathVariable Long demandeId) {
        InsuranceRecommendation recommendation = insuranceService.calculateRecommendation(demandeId);
        return ResponseEntity.ok(InsuranceMapper.toRecommendationDTO(recommendation));
    }

    @GetMapping("/offers")
    public ResponseEntity<List<InsuranceOfferDTO>> getAllOffers() {
        List<InsuranceOfferDTO> dtos = insuranceService.getAllOffers()
                .stream()
                .map(InsuranceMapper::toOfferDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<InsuranceCompanyDTO>> getAllCompanies() {
        List<InsuranceCompanyDTO> dtos = insuranceService.getAllCompanies()
                .stream()
                .map(InsuranceMapper::toCompanyDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<InsurancePolicyDTO> subscribe(@RequestParam Long clientId, @RequestParam Long offerId, @RequestParam Long demandeId) {
        InsurancePolicy policy = insuranceService.subscribeToOffer(clientId, offerId, demandeId);
        return ResponseEntity.ok(InsurancePolicyMapper.toDTO(policy));
    }

    @PostMapping("/claims")
    public ResponseEntity<InsuranceClaimDTO> submitClaim(@RequestParam Long policyId, @RequestParam String reason, @RequestParam(required = false) String reference) {
        InsuranceClaim claim = insuranceService.submitClaim(policyId, reason, reference);
        return ResponseEntity.ok(InsuranceClaimMapper.toDTO(claim));
    }

    @GetMapping("/claims/client/{clientId}")
    public ResponseEntity<List<InsuranceClaimDTO>> getClientClaims(@PathVariable Long clientId) {
        List<InsuranceClaimDTO> dtos = insuranceService.getClaimsByClient(clientId)
                .stream()
                .map(InsuranceClaimMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/claims/{claimId}/status")
    public ResponseEntity<InsuranceClaimDTO> updateClaimStatus(@PathVariable Long claimId, @RequestParam ClaimStatus status, @RequestParam(required = false) String reason) {
        InsuranceClaim claim = insuranceService.updateClaimStatus(claimId, status, reason);
        return ResponseEntity.ok(InsuranceClaimMapper.toDTO(claim));
    }

    @GetMapping("/client/{clientId}/risk-score")
    public ResponseEntity<Double> getClientRiskScore(@PathVariable Long clientId) {
        return ResponseEntity.ok(insuranceService.calculateRiskScore(clientId));
    }

    @GetMapping("/offers/{offerId}/adequacy/{clientId}")
    public ResponseEntity<Integer> getAdequacyScore(@PathVariable Long offerId, @PathVariable Long clientId) {
        return ResponseEntity.ok(insuranceService.calculateAdequacyScore(offerId, clientId));
    }

    @GetMapping("/policies/{policyId}/pdf")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Long policyId) {
        return ResponseEntity.ok(insuranceService.generatePolicyPDF(policyId));
    }

    @PostMapping("/chatbot/ask")
    public ResponseEntity<ChatbotResponseDTO> askChatbot(@RequestBody ChatbotRequestDTO request) {
        log.info("Chatbot questioning: {}", request.getQuestion());
        String question = request.getQuestion() != null ? request.getQuestion() : "";
        Long clientId = request.getClientId();
        
        // 1. Try to get answer from our new RAG AI Service
        String aiAnswer = insuranceRagService.askInsuranceAI(question);
        
        if (aiAnswer != null && !aiAnswer.isEmpty()) {
            return ResponseEntity.ok(new ChatbotResponseDTO(aiAnswer, "AI_RAG", "SUCCESS", "NONE"));
        }

        // 2. Fallback to old hardcoded logic if AI is offline or fails
        String sector = "Inconnu";
        Double riskScore = 50.0;
        if (clientId != null) {
            User user = userRepository.findById(clientId).orElse(null);
            if (user != null) {
                sector = user.getSector() != null ? user.getSector() : "Général";
                riskScore = insuranceService.calculateRiskScore(clientId);
            }
        }

        String responseMsg;
        String intent = "GENERAL";
        String action = "NONE";

        String qLower = question.toLowerCase();
        if (qLower.contains("recommandation") || qLower.contains("conseil")) {
            responseMsg = String.format("Basé sur votre secteur (%s) et votre score de risque (%.0f), je vous suggère de regarder nos offres avec une couverture spécifique. Souhaitez-vous une simulation ?", sector, riskScore);
            intent = "RECOMMENDATION";
            action = "ASK_CONFIRMATION";
        } else if (qLower.contains("oui") || qLower.contains("ok") || qLower.contains("simule")) {
            responseMsg = "Super ! Utilisons vos données de profil pour la simulation. Quel montant souhaitez-vous assurer ?";
            intent = "SIMULATION_START";
            action = "START_SIMULATION";
        } else if (qLower.contains("partenaire") || qLower.contains("compagnie")) {
            responseMsg = "Nous travaillons avec STAR, COMAR et GAT Assurances. STAR est excellente pour le transport, tandis que COMAR est leader en assurance-crédit.";
            intent = "PARTNERS";
            action = "SHOW_PARTNERS";
        } else if (qLower.contains("sinistre") || qLower.contains("déclarer")) {
            responseMsg = "Vous pouvez déclarer un sinistre via l'onglet 'Mes Claims'. Voulez-vous que je vous y emmène ?";
            intent = "CLAIMS";
            action = "GO_TO_CLAIMS";
        } else if (qLower.contains("prix") || qLower.contains("tarif") || qLower.contains("coût")) {
            responseMsg = String.format("Pour un profil à %.0f%% de risque, nos primes commencent généralement à 150 DT par an.", riskScore);
            intent = "PRICING";
            action = "SHOW_OFFERS";
        } else {
            responseMsg = "Je suis votre assistant CrediGuard ! Je connais votre profil et je peux vous aider pour une recommandation d'assurance ou le suivi de vos sinistres. Que puis-je faire ?";
        }

        return ResponseEntity.ok(new ChatbotResponseDTO(responseMsg, intent, "SUCCESS", action));
    }
}

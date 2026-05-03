package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import tn.esprit.pi_back.services.IInsuranceService;
import tn.esprit.pi_back.dto.insurance.*;

import java.util.List;

@RestController
@RequestMapping("/insurance")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class InsuranceController {

    private final IInsuranceService insuranceService;

    @PostMapping("/simulate")
    public ResponseEntity<InsuranceRecommendation> simulate(@RequestBody InsuranceSimulationDTO simulationRequest) {
        return ResponseEntity.ok(insuranceService.simulateInsurance(simulationRequest));
    }

    @GetMapping("/recommendations/client/{clientId}")
    public ResponseEntity<List<InsuranceRecommendation>> getRecommendationsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(insuranceService.getRecommendationsByClient(clientId));
    }

    @PostMapping("/recommendation/{demandeId}")
    public ResponseEntity<InsuranceRecommendation> getRecommendation(@PathVariable Long demandeId) {
        return ResponseEntity.ok(insuranceService.calculateRecommendation(demandeId));
    }

    @GetMapping("/offers")
    public ResponseEntity<List<InsuranceOffer>> getAllOffers() {
        return ResponseEntity.ok(insuranceService.getAllOffers());
    }

    @GetMapping("/companies")
    public ResponseEntity<List<InsuranceCompany>> getAllCompanies() {
        return ResponseEntity.ok(insuranceService.getAllCompanies());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<InsurancePolicy> subscribe(@RequestParam Long clientId, @RequestParam Long offerId, @RequestParam Long demandeId) {
        return ResponseEntity.ok(insuranceService.subscribeToOffer(clientId, offerId, demandeId));
    }

    @PostMapping("/claims")
    public ResponseEntity<InsuranceClaim> submitClaim(@RequestParam Long policyId, @RequestParam String reason, @RequestParam(required = false) String reference) {
        return ResponseEntity.ok(insuranceService.submitClaim(policyId, reason, reference));
    }

    @GetMapping("/claims/client/{clientId}")
    public ResponseEntity<List<InsuranceClaim>> getClientClaims(@PathVariable Long clientId) {
        return ResponseEntity.ok(insuranceService.getClaimsByClient(clientId));
    }

    @PutMapping("/claims/{claimId}/status")
    public ResponseEntity<InsuranceClaim> updateClaimStatus(@PathVariable Long claimId, @RequestParam ClaimStatus status, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(insuranceService.updateClaimStatus(claimId, status, reason));
    }
    @PostMapping("/chatbot/ask")
    public ResponseEntity<ChatbotResponseDTO> askChatbot(@RequestBody ChatbotRequestDTO request) {
        log.info("Chatbot questioning: {}", request.getQuestion());
        String question = request.getQuestion() != null ? request.getQuestion().toLowerCase() : "";
        String responseMsg;
        String intent = "GENERAL";
        String action = "NONE";

        if (question.contains("recommandation") || question.contains("conseil")) {
            responseMsg = "D'après les analyses de risques courantes, je vous suggère de regarder nos offres avec une couverture 'Premium' si votre prêt dépasse 50k DT. Souhaitez-vous que je simule une offre personnalisée pour vous ?";
            intent = "RECOMMENDATION";
            action = "ASK_CONFIRMATION";
        } else if (question.equals("oui") || question.equals("yes") || question.contains("d'accord") || question.contains("ok") || question.contains("simule")) {
            responseMsg = "Super ! Pour commencer la simulation, j'ai besoin de quelques détails. Quel serait le **montant** du crédit que vous souhaitez assurer ?";
            intent = "SIMULATION_START";
            action = "START_SIMULATION";
        } else if (question.contains("partenaire") || question.contains("compagnie")) {
            responseMsg = "Nous travaillons avec plusieurs partenaires de confiance comme STAR, COMAR et GAT Assurances. Vous pouvez voir la liste complète dans l'onglet 'Partenaires'.";
            intent = "PARTNERS";
            action = "SHOW_PARTNERS";
        } else if (question.contains("sinistre") || question.contains("claim") || question.contains("déclarer")) {
            responseMsg = "Pour déclarer un sinistre, vous pouvez vous rendre dans votre espace client sous la rubrique 'Mes Polices' et cliquer sur 'Déposer une plainte'. Avez-vous votre numéro de police ?";
            intent = "CLAIMS";
        } else if (question.contains("prix") || question.contains("tarif") || question.contains("coût")) {
            responseMsg = "Nos tarifs dépendent de votre profil de risque. En général, les primes annuelles varient entre 100 et 600 DT selon la couverture choisie. Souhaitez-vous voir nos offres ?";
            intent = "PRICING";
            action = "SHOW_OFFERS";
        } else {
            responseMsg = "Je suis votre assistant CrediGuard ! Je peux vous aider pour une recommandation d'assurance, la liste de nos partenaires ou le suivi de vos sinistres. Que puis-je faire pour vous ?";
        }

        return ResponseEntity.ok(new ChatbotResponseDTO(responseMsg, intent, "SUCCESS", action));
    }

}

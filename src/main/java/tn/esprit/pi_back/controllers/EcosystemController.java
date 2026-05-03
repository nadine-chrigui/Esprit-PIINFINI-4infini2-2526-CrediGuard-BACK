package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.services.CardEcosystemService;

import java.util.Map;
import java.util.List;
import tn.esprit.pi_back.entities.Carte;
import tn.esprit.pi_back.entities.Cashback;
import tn.esprit.pi_back.entities.FinancialScore;
import tn.esprit.pi_back.entities.AiInsight;

@RestController
@RequestMapping("/ecosystem")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EcosystemController {

    private final CardEcosystemService ecosystemService;

    @PostMapping("/payment")
    public ResponseEntity<?> payWithCard(@RequestBody Map<String, Object> payload) {
        try {
            String cardNumber = (String) payload.get("cardNumber");
            Double amount = ((Number) payload.get("amount")).doubleValue();
            String merchant = (String) payload.get("merchant");
            String category = (String) payload.get("category");

            Transaction tx = ecosystemService.processPayment(cardNumber, amount, merchant, category);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Payment processed successfully",
                    "transactionId", tx.getIdTransaction(),
                    "montant", tx.getMontant()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown payment error"
            ));
        }
    }

    @GetMapping("/score/{userId}")
    public ResponseEntity<FinancialScore> getScore(@PathVariable Long userId) {
        return ResponseEntity.ok(ecosystemService.getScoreForUser(userId));
    }

    @GetMapping("/insights/{userId}")
    public ResponseEntity<List<AiInsight>> getInsights(@PathVariable Long userId) {
        return ResponseEntity.ok(ecosystemService.getInsightsForUser(userId));
    }

    @GetMapping("/card/{compteId}")
    public ResponseEntity<Carte> getCardByCompte(@PathVariable Long compteId) {
        return ResponseEntity.ok(ecosystemService.getCardByCompte(compteId));
    }

    @GetMapping("/cashbacks/{compteId}")
    public ResponseEntity<List<Cashback>> getCashbacksByCompte(@PathVariable Long compteId) {
        return ResponseEntity.ok(ecosystemService.getCashbacksByCompte(compteId));
    }
}
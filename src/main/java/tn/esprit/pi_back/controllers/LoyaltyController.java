package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.CashbackTransaction;
import tn.esprit.pi_back.entities.LoyaltyAccount;
import tn.esprit.pi_back.entities.enums.CashbackTransactionType;
import tn.esprit.pi_back.services.LoyaltyService;
import tn.esprit.pi_back.services.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final UserService userService;

    @GetMapping("/account")
    public ResponseEntity<LoyaltyAccount> getMyAccount() {
        Long userId = userService.getCurrentUserOrThrow().getId();
        return ResponseEntity.ok(loyaltyService.getLoyaltyAccount(userId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<CashbackTransaction>> getMyTransactions() {
        Long userId = userService.getCurrentUserOrThrow().getId();
        return ResponseEntity.ok(loyaltyService.getTransactions(userId));
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCashback(@RequestBody Map<String, Object> payload) {
        Long userId = userService.getCurrentUserOrThrow().getId();
        double amount = Double.parseDouble(payload.get("amount").toString());
        String typeStr = payload.get("type").toString();
        String description = payload.getOrDefault("description", "Cashback Redemption").toString();

        CashbackTransactionType type = CashbackTransactionType.valueOf(typeStr);
        loyaltyService.redeemCashback(userId, amount, type, description);
        
        return ResponseEntity.ok(Map.of("message", "Cashback redeemed successfully"));
    }

    // For testing/simulation
    @PostMapping("/simulate-purchase")
    public ResponseEntity<?> simulatePurchase(@RequestBody Map<String, Object> payload) {
        Long userId = userService.getCurrentUserOrThrow().getId();
        double amount = Double.parseDouble(payload.get("amount").toString());
        String description = payload.getOrDefault("description", "Achat par carte").toString();

        loyaltyService.attributeCashback(userId, amount, description);
        return ResponseEntity.ok(Map.of("message", "Purchase simulated and cashback attributed"));
    }
}

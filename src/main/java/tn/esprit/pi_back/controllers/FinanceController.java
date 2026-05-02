package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.repositories.CreditRepository;
import tn.esprit.pi_back.repositories.TransactionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FinanceController {

    private final TransactionRepository transactionRepository;
    private final CreditRepository creditRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        List<Transaction> transactions = transactionRepository.findAll();

        double totalRevenue = transactions.stream()
                .filter(t -> t.getTypeTransaction().name().equals("VENTE"))
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(t -> t.getTypeTransaction().name().equals("ACHAT"))
                .mapToDouble(Transaction::getMontant)
                .sum();

        summary.put("totalTransactions", transactions.size());
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalExpenses", totalExpenses);
        summary.put("totalAccounts", 0);
        summary.put("pendingTransactions", transactions.stream()
                .filter(t -> t.getStatut().name().equals("PENDING")).count());
        summary.put("totalRemboursements", 0);
        summary.put("totalUsers", 0);
        summary.put("totalAdmins", 0);
        summary.put("totalBeneficiaries", 0);
        summary.put("totalPartners", 0);
        summary.put("totalCredits", creditRepository.count());
        summary.put("activeCredits", 0);
        summary.put("closedCredits", 0);
        summary.put("totalAmountGranted", 0);
        summary.put("totalAmountRemaining", 0);
        summary.put("revenueTrend", 0);
        summary.put("expenseTrend", 0);
        summary.put("monthlyRevenue", new HashMap<>());
        summary.put("monthlyExpenses", new HashMap<>());
        summary.put("activeAlerts", List.of());
        summary.put("forecastedRevenue", 0);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/credits")
    public ResponseEntity<List<Credit>> getCredits() {
        return ResponseEntity.ok(creditRepository.findAll());
    }
}
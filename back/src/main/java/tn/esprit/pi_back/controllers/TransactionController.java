package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.TransactionRequest;
import tn.esprit.pi_back.dto.TransactionResponse;
import tn.esprit.pi_back.services.FinanceService;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(financeService.getTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getTransaction(id));
    }

    @GetMapping("/compte/{compteId}")
    public ResponseEntity<List<TransactionResponse>> getByCompte(@PathVariable Long compteId) {
        return ResponseEntity.ok(financeService.getTransactionsByCompte(compteId));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(financeService.createTransaction(request));
    }
}

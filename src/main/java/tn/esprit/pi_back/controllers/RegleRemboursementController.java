package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.RegleRemboursementRequest;
import tn.esprit.pi_back.dto.RegleRemboursementResponse;
import tn.esprit.pi_back.services.FinanceService;

import java.util.List;

@RestController
@RequestMapping("/regles-remboursement")
@RequiredArgsConstructor
public class RegleRemboursementController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<RegleRemboursementResponse>> getAll() {
        return ResponseEntity.ok(financeService.getRegles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegleRemboursementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getRegle(id));
    }

    @GetMapping("/credit/{creditId}")
    public ResponseEntity<List<RegleRemboursementResponse>> getByCredit(@PathVariable Long creditId) {
        return ResponseEntity.ok(financeService.getReglesByCredit(creditId));
    }

    @PostMapping
    public ResponseEntity<RegleRemboursementResponse> create(@Valid @RequestBody RegleRemboursementRequest request) {
        return ResponseEntity.ok(financeService.createRegle(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegleRemboursementResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RegleRemboursementRequest request
    ) {
        return ResponseEntity.ok(financeService.updateRegle(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        financeService.deleteRegle(id);
        return ResponseEntity.noContent().build();
    }
}

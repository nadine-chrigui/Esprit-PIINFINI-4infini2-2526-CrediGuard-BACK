package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.RemboursementRequest;
import tn.esprit.pi_back.dto.RemboursementResponse;
import tn.esprit.pi_back.services.FinanceService;

import java.util.List;

@RestController
@RequestMapping("/remboursements")
@RequiredArgsConstructor
public class RemboursementController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<RemboursementResponse>> getAll() {
        return ResponseEntity.ok(financeService.getRemboursements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RemboursementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getRemboursement(id));
    }

    @GetMapping("/credit/{creditId}")
    public ResponseEntity<List<RemboursementResponse>> getByCredit(@PathVariable Long creditId) {
        return ResponseEntity.ok(financeService.getRemboursementsByCredit(creditId));
    }

    @PostMapping
    public ResponseEntity<RemboursementResponse> create(@Valid @RequestBody RemboursementRequest request) {
        return ResponseEntity.ok(financeService.createRemboursement(request));
    }
}

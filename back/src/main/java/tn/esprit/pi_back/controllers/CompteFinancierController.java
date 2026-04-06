package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.CompteFinancierRequest;
import tn.esprit.pi_back.dto.CompteFinancierResponse;
import tn.esprit.pi_back.services.FinanceService;

import java.util.List;

@RestController
@RequestMapping("/comptes-financiers")
@RequiredArgsConstructor
public class CompteFinancierController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<CompteFinancierResponse>> getAll() {
        return ResponseEntity.ok(financeService.getComptes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompteFinancierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getCompte(id));
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<CompteFinancierResponse> getByUtilisateur(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(financeService.getCompteByUtilisateurId(utilisateurId));
    }

    @PostMapping
    public ResponseEntity<CompteFinancierResponse> create(@Valid @RequestBody CompteFinancierRequest request) {
        return ResponseEntity.ok(financeService.createCompte(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompteFinancierResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CompteFinancierRequest request
    ) {
        return ResponseEntity.ok(financeService.updateCompte(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        financeService.deleteCompte(id);
        return ResponseEntity.noContent().build();
    }
}

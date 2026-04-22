package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.CompteFinancier;
import tn.esprit.pi_back.services.CompteFinancierService;

import java.util.List;

@RestController
@RequestMapping("/api/comptes-financiers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompteFinancierController {

    private final CompteFinancierService compteFinancierService;

    @PostMapping
    public ResponseEntity<CompteFinancier> create(@Valid @RequestBody CompteFinancier compte) {
        return ResponseEntity.ok(compteFinancierService.create(compte));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompteFinancier> update(@PathVariable Long id, @Valid @RequestBody CompteFinancier compte) {
        return ResponseEntity.ok(compteFinancierService.update(id, compte));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompteFinancier> getById(@PathVariable Long id) {
        return ResponseEntity.ok(compteFinancierService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CompteFinancier>> getAll() {
        return ResponseEntity.ok(compteFinancierService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        compteFinancierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.RegleRemboursement;
import tn.esprit.pi_back.services.RegleRemboursementService;

import java.util.List;

@RestController
@RequestMapping("/api/regles-remboursement")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegleRemboursementController {

    private final RegleRemboursementService regleRemboursementService;

    @PostMapping
    public ResponseEntity<RegleRemboursement> create(@Valid @RequestBody RegleRemboursement regle) {
        return ResponseEntity.ok(regleRemboursementService.create(regle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegleRemboursement> update(@PathVariable Long id,
            @Valid @RequestBody RegleRemboursement regle) {
        return ResponseEntity.ok(regleRemboursementService.update(id, regle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegleRemboursement> getById(@PathVariable Long id) {
        return ResponseEntity.ok(regleRemboursementService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RegleRemboursement>> getAll() {
        return ResponseEntity.ok(regleRemboursementService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        regleRemboursementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Remboursement;
import tn.esprit.pi_back.services.RemboursementService;

import java.util.List;

@RestController
@RequestMapping("/remboursements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RemboursementController {

    private final RemboursementService remboursementService;

    @PostMapping
    public ResponseEntity<Remboursement> create(@Valid @RequestBody Remboursement remboursement) {
        return ResponseEntity.ok(remboursementService.create(remboursement));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Remboursement> update(@PathVariable Long id,
            @Valid @RequestBody Remboursement remboursement) {
        return ResponseEntity.ok(remboursementService.update(id, remboursement));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Remboursement> getById(@PathVariable Long id) {
        return ResponseEntity.ok(remboursementService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Remboursement>> getAll() {
        return ResponseEntity.ok(remboursementService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        remboursementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Vente;
import tn.esprit.pi_back.services.VenteService;

import java.util.List;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VenteController {

    private final VenteService venteService;

    @PostMapping
    public ResponseEntity<Vente> create(@Valid @RequestBody Vente vente) {
        return ResponseEntity.ok(venteService.create(vente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vente> update(@PathVariable Long id, @Valid @RequestBody Vente vente) {
        return ResponseEntity.ok(venteService.update(id, vente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vente> getById(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Vente>> getAll() {
        return ResponseEntity.ok(venteService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

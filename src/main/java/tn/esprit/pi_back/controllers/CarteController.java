package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Carte;
import tn.esprit.pi_back.services.CarteService;

import java.util.List;

@RestController
@RequestMapping("/cartes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CarteController {

    private final CarteService carteService;

    @GetMapping
    public ResponseEntity<List<Carte>> getAllCartes() {
        return ResponseEntity.ok(carteService.getAllCartes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carte> getCarteById(@PathVariable Long id) {
        return carteService.getCarteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Carte> createCarte(@RequestBody Carte carte) {
        return ResponseEntity.ok(carteService.createCarte(carte));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carte> updateCarte(@PathVariable Long id, @RequestBody Carte carte) {
        try {
            return ResponseEntity.ok(carteService.updateCarte(id, carte));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarte(@PathVariable Long id) {
        carteService.deleteCarte(id);
        return ResponseEntity.noContent().build();
    }
}

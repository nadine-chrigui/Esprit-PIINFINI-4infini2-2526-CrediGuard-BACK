package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.InsuranceOfferDTO;
import tn.esprit.pi_back.dto.insurance.RecommendedOfferDTO;
import tn.esprit.pi_back.entities.InsuranceOffer;
import tn.esprit.pi_back.services.IInsuranceOfferService;

import java.util.List;

@RestController
@RequestMapping("/offres")
@RequiredArgsConstructor
@CrossOrigin("*")
public class OffreController {

    private final IInsuranceOfferService offerService;

    @GetMapping
    public ResponseEntity<List<InsuranceOfferDTO>> getAll() {
        return ResponseEntity.ok(offerService.getAll());
    }

    @GetMapping("/recommandees")
    public ResponseEntity<List<RecommendedOfferDTO>> getRecommended(@RequestParam Long clientId) {
        return ResponseEntity.ok(offerService.getRecommended(clientId));
    }

    @PostMapping
    public ResponseEntity<InsuranceOffer> create(@RequestBody InsuranceOffer offer) {
        return ResponseEntity.ok(offerService.save(offer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsuranceOffer> update(@PathVariable Long id, @RequestBody InsuranceOffer offer) {
        return ResponseEntity.ok(offerService.update(id, offer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

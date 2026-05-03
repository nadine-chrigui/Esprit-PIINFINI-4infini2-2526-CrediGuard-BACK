package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Vente;
import tn.esprit.pi_back.services.VenteService;

import java.util.List;

@RestController
@RequestMapping("/ventes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class VenteController {

    private final VenteService venteService;

    @PostMapping
    public Vente create(@RequestBody Vente vente) {
        return venteService.create(vente);
    }

    @PutMapping("/{id}")
    public Vente update(@PathVariable Long id, @RequestBody Vente vente) {
        return venteService.update(id, vente);
    }

    @GetMapping("/{id}")
    public Vente getById(@PathVariable Long id) {
        return venteService.getById(id);
    }

    @GetMapping
    public List<Vente> getAll() {
        return venteService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        venteService.delete(id);
    }
}

package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import tn.esprit.pi_back.dto.demande.DemandeCreditRequestDTO;
import tn.esprit.pi_back.dto.demande.DemandeCreditResponseDTO;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.services.DemandeCreditService;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
public class DemandeCreditController {

    private final DemandeCreditService service;

    // CREATE
    @PostMapping
    public DemandeCreditResponseDTO create(
            @RequestParam Long clientId,
            @Valid @RequestBody DemandeCreditRequestDTO dto
    ) {
        return service.create(clientId, dto);
    }

    // READ one
    @GetMapping("/{id}")
    public DemandeCreditResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // READ list + filters
    @GetMapping
    public List<DemandeCreditResponseDTO> getAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) StatutDemande statut
    ) {
        return service.getAll(clientId, statut);
    }

    // UPDATE
    @PutMapping("/{id}")
    public DemandeCreditResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody DemandeCreditRequestDTO dto
    ) {
        return service.update(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // PATCH status (workflow)
    @PatchMapping("/{id}/status")
    public DemandeCreditResponseDTO setStatus(
            @PathVariable Long id,
            @RequestParam StatutDemande statut
    ) {
        return service.setStatus(id, statut);
    }
}
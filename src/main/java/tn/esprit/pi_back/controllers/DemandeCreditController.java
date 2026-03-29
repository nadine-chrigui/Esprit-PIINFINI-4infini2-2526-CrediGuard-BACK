package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.demande.DemandeCreditRequestDTO;
import tn.esprit.pi_back.dto.demande.DemandeCreditResponseDTO;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.services.DemandeCreditService;

import java.util.List;

@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeCreditController {

    private final DemandeCreditService service;

    @PostMapping
    public DemandeCreditResponseDTO create(
            Authentication authentication,
            @Valid @RequestBody DemandeCreditRequestDTO dto
    ) {
        String email = authentication.getName();
        return service.create(email, dto);
    }

    @GetMapping("/{id}")
    public DemandeCreditResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<DemandeCreditResponseDTO> getAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) StatutDemande statut
    ) {
        return service.getAll(clientId, statut);
    }

    @PutMapping("/{id}")
    public DemandeCreditResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody DemandeCreditRequestDTO dto
    ) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/status")
    public DemandeCreditResponseDTO setStatus(
            @PathVariable Long id,
            @RequestParam StatutDemande statut
    ) {
        return service.setStatus(id, statut);
    }
}
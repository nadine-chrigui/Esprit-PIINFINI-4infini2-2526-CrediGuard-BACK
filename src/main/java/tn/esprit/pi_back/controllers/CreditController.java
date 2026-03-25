package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.credit.*;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import tn.esprit.pi_back.services.CreditService;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService service;

    @PostMapping
    public CreditResponseDTO create(
            @RequestParam Long demandeId,
            @Valid @RequestBody CreditRequestDTO dto
    ) {
        return service.create(demandeId, dto);
    }

    @GetMapping("/{id}")
    public CreditResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<CreditResponseDTO> getAll(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) StatutCredit statut
    ) {
        return service.getAll(clientId, statut);
    }

    @PutMapping("/{id}")
    public CreditResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody CreditRequestDTO dto
    ) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/status")
    public CreditResponseDTO changeStatus(
            @PathVariable Long id,
            @RequestParam StatutCredit statut
    ) {
        return service.changeStatus(id, statut);
    }
}
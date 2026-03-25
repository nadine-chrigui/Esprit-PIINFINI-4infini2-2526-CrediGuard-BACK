package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.decision.*;
import tn.esprit.pi_back.services.DecisionCreditService;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionCreditController {

    private final DecisionCreditService service;

    @PostMapping
    public DecisionCreditResponseDTO create(
            @RequestParam Long demandeId,
            @Valid @RequestBody DecisionCreditRequestDTO dto
    ) {
        return service.create(demandeId, dto);
    }

    @GetMapping
    public DecisionCreditResponseDTO getByDemande(@RequestParam Long demandeId) {
        return service.getByDemande(demandeId);
    }
}
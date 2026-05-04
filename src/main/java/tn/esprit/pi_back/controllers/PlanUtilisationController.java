package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.plan.*;
import tn.esprit.pi_back.services.PlanUtilisationService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanUtilisationController {

    private final PlanUtilisationService service;

    @PostMapping
    public PlanUtilisationResponseDTO create(
            @RequestParam Long demandeId,
            @Valid @RequestBody PlanUtilisationRequestDTO dto
    ) {
        return service.create(demandeId, dto);
    }

    @GetMapping
    public PlanUtilisationResponseDTO get(@RequestParam Long demandeId) {
        return service.getByDemande(demandeId);
    }

    @PutMapping
    public PlanUtilisationResponseDTO update(
            @RequestParam Long demandeId,
            @Valid @RequestBody PlanUtilisationRequestDTO dto
    ) {
        return service.update(demandeId, dto);
    }

    @GetMapping("/mine")
    public List<PlanUtilisationResponseDTO> getMine(@RequestParam Long clientId) {
        return service.getMine(clientId);
    }
}
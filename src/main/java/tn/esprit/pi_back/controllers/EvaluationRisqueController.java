package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.evaluation.EvaluationPredictionRequestDTO;
import tn.esprit.pi_back.dto.evaluation.EvaluationRisqueRequestDTO;
import tn.esprit.pi_back.dto.evaluation.EvaluationRisqueResponseDTO;
import tn.esprit.pi_back.services.EvaluationRisqueService;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
public class EvaluationRisqueController {

    private final EvaluationRisqueService service;

    @PostMapping
    public EvaluationRisqueResponseDTO create(
            @RequestParam Long demandeId,
            @Valid @RequestBody EvaluationRisqueRequestDTO dto
    ) {
        return service.create(demandeId, dto);
    }

    @PostMapping("/predict-mc")
    public EvaluationRisqueResponseDTO predictWithModel(
            @RequestParam Long demandeId,
            @Valid @RequestBody EvaluationPredictionRequestDTO dto
    ) {
        return service.predictWithModel(demandeId, dto);
    }

    @GetMapping
    public EvaluationRisqueResponseDTO getByDemande(@RequestParam Long demandeId) {
        return service.getByDemande(demandeId);
    }
}

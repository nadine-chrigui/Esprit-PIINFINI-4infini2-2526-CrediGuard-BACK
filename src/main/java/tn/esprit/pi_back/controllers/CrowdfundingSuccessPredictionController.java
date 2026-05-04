package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.Ml.CrowdfundingSuccessPredictionResponse;
import tn.esprit.pi_back.services.CrowdfundingSuccessPredictionService;

@RestController
@RequestMapping("/project-success-predictions")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CrowdfundingSuccessPredictionController {

    private final CrowdfundingSuccessPredictionService predictionService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<CrowdfundingSuccessPredictionResponse> getLatestPrediction(@PathVariable Long projectId) {
        return ResponseEntity.ok(predictionService.getLatestPrediction(projectId));
    }

    @PostMapping("/project/{projectId}/refresh")
    public ResponseEntity<CrowdfundingSuccessPredictionResponse> refreshPrediction(@PathVariable Long projectId) {
        return ResponseEntity.ok(predictionService.refreshPrediction(projectId));
    }
}

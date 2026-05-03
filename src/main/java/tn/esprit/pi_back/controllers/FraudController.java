package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.FraudAnalysisResult;
import tn.esprit.pi_back.services.FraudDetectionService;

@RestController
@RequestMapping("/fraud")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FraudController {

    private final FraudDetectionService fraudService;

    @GetMapping("/result/{txId}")
    public ResponseEntity<FraudAnalysisResult> getResult(@PathVariable Long txId) {
        FraudAnalysisResult result = fraudService.getResultForTransaction(txId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}

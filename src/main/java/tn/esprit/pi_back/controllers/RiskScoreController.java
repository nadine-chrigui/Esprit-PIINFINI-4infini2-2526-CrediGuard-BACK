package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.InsuranceMapper;
import tn.esprit.pi_back.dto.insurance.RiskScoreDTO;
import tn.esprit.pi_back.entities.RiskScore;
import tn.esprit.pi_back.services.IRiskScoreService;

@RestController
@RequestMapping("/score-risque")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class RiskScoreController {

    private final IRiskScoreService riskScoreService;

    @PostMapping("/calculer")
    public ResponseEntity<RiskScoreDTO> calculate(@RequestParam Long userId,
                                             @RequestParam String sector,
                                             @RequestParam String region,
                                             @RequestParam Double value,
                                             @RequestParam String goodsNature) {
        log.info("Calculation request received for user: {}", userId);
        RiskScore score = riskScoreService.calculate(userId, sector, region, value, goodsNature);
        return ResponseEntity.ok(InsuranceMapper.toRiskScoreDTO(score));
    }

    @PostMapping("/simuler")
    public ResponseEntity<RiskScoreDTO> simulate(@RequestParam String sector,
                                            @RequestParam String region,
                                            @RequestParam Double value,
                                            @RequestParam String goodsNature) {
        log.info("===> SIMULATION REQUEST RECEIVED: sector={}, region={}, value={}", sector, region, value);
        RiskScore score = riskScoreService.simulate(sector, region, value, goodsNature);
        log.info("===> SIMULATION SCORE COMPUTED: {}", score.getGlobalScore());
        return ResponseEntity.ok(InsuranceMapper.toRiskScoreDTO(score));
    }
}

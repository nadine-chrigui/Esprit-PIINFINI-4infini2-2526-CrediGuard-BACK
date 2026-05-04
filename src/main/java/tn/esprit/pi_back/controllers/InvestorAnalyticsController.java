package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.InvestorAnalytics.InvestorAnalyticsResponse;
import tn.esprit.pi_back.services.InvestorAnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/investor-analytics")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InvestorAnalyticsController {

    private final InvestorAnalyticsService investorAnalyticsService;

    @GetMapping("/investor/{investorId}/latest")
    public ResponseEntity<InvestorAnalyticsResponse> getLatest(@PathVariable Long investorId) {
        return ResponseEntity.ok(investorAnalyticsService.getLatestByInvestor(investorId));
    }

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<InvestorAnalyticsResponse>> getHistory(@PathVariable Long investorId) {
        return ResponseEntity.ok(investorAnalyticsService.getHistoryByInvestor(investorId));
    }
}

package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi_back.dto.CreditLookupResponse;
import tn.esprit.pi_back.dto.FinanceSummaryResponse;
import tn.esprit.pi_back.services.FinanceService;

import java.util.List;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryResponse> getSummary() {
        return ResponseEntity.ok(financeService.getFinanceSummary());
    }

    @GetMapping("/credits")
    public ResponseEntity<List<CreditLookupResponse>> getCredits() {
        return ResponseEntity.ok(financeService.getCreditsLookup());
    }
}

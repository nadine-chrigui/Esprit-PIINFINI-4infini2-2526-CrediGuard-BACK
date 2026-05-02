package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.services.FinancialAdvisorService;

import java.util.Map;

@RestController
@RequestMapping("/financial-advisor")
@RequiredArgsConstructor
public class FinancialAdvisorController {

    private final FinancialAdvisorService service;

    @GetMapping
    public Map<String, Object> getAdvisorReport(@RequestParam Long demandeId) {
        return service.getAdvisorReport(demandeId);
    }
}

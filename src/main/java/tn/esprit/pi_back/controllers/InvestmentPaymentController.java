package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentIntentRequest;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentIntentResponse;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentResponse;
import tn.esprit.pi_back.services.InvestmentPaymentService;

import java.util.List;

@RestController
@RequestMapping("/investment-payments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InvestmentPaymentController {

    private final InvestmentPaymentService investmentPaymentService;

    @PostMapping("/intents")
    public ResponseEntity<InvestmentPaymentIntentResponse> createPaymentIntent(@Valid @RequestBody InvestmentPaymentIntentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentPaymentService.createPaymentIntent(request));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<InvestmentPaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(investmentPaymentService.getById(id));
    }

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<InvestmentPaymentResponse>> getByInvestor(@PathVariable Long investorId) {
        return ResponseEntity.ok(investmentPaymentService.getByInvestor(investorId));
    }
}

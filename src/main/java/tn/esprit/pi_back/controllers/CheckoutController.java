package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.checkout.CheckoutRequest;
import tn.esprit.pi_back.dto.checkout.CheckoutResponse;
import tn.esprit.pi_back.services.CheckoutService;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest req) {
        return ResponseEntity.ok(checkoutService.checkout(req));
    }
}
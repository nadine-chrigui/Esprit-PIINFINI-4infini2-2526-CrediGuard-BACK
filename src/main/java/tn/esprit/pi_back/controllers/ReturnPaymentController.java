package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.ReturnPayment.*;
import tn.esprit.pi_back.services.ReturnPaymentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/return-payments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReturnPaymentController {

    private final ReturnPaymentService returnPaymentService;

    @PostMapping
    public ResponseEntity<ReturnPaymentResponse> create(@Valid @RequestBody ReturnPaymentCreateRequest req) {
        ReturnPaymentResponse created = returnPaymentService.create(req);
        return ResponseEntity.created(URI.create("/api/return-payments/" + created.returnId())).body(created);
    }

    @PostMapping("/intents")
    public ResponseEntity<ReturnPaymentIntentResponse> createPaymentIntent(@Valid @RequestBody ReturnPaymentIntentRequest req) {
        return ResponseEntity.ok(returnPaymentService.createPaymentIntent(req));
    }

    @GetMapping
    public ResponseEntity<List<ReturnPaymentResponse>> getAll() {
        return ResponseEntity.ok(returnPaymentService.getAll());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ReturnPaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(returnPaymentService.getById(id));
    }

    @GetMapping("/investment/{investmentId}")
    public ResponseEntity<List<ReturnPaymentResponse>> getByInvestment(@PathVariable Long investmentId) {
        return ResponseEntity.ok(returnPaymentService.getByInvestment(investmentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReturnPaymentResponse> update(@PathVariable Long id,
                                                        @RequestBody ReturnPaymentUpdateRequest req) {
        return ResponseEntity.ok(returnPaymentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        returnPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

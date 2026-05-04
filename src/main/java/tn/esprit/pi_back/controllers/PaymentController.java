package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.Payment.PaymentCreateRequest;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.Payment.PaymentUpdateRequest;
import tn.esprit.pi_back.services.PaymentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentCreateRequest req) {
        PaymentResponse created = paymentService.create(req);
        return ResponseEntity.created(URI.create("/api/payments/" + created.id())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrder(orderId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> update(@PathVariable Long id, @RequestBody PaymentUpdateRequest req) {
        return ResponseEntity.ok(paymentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
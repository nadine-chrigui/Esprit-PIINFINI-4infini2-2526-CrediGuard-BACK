package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.OptionRedemption.*;
import tn.esprit.pi_back.services.OptionRedemptionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/option-redemptions")
@RequiredArgsConstructor
@CrossOrigin("*")
public class OptionRedemptionController {

    private final OptionRedemptionService optionRedemptionService;

    @PostMapping
    public ResponseEntity<OptionRedemptionResponse> create(@Valid @RequestBody OptionRedemptionCreateRequest req) {
        OptionRedemptionResponse created = optionRedemptionService.create(req);
        return ResponseEntity.created(URI.create("/api/option-redemptions/" + created.redemptionId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<OptionRedemptionResponse>> getAll() {
        return ResponseEntity.ok(optionRedemptionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OptionRedemptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(optionRedemptionService.getById(id));
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<OptionRedemptionResponse> getBySubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(optionRedemptionService.getBySubscription(subscriptionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        optionRedemptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

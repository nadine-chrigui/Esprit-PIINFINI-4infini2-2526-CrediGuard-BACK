package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.OptionSubscription.*;
import tn.esprit.pi_back.services.OptionSubscriptionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/option-subscriptions")
@RequiredArgsConstructor
@CrossOrigin("*")
public class OptionSubscriptionController {

    private final OptionSubscriptionService optionSubscriptionService;

    @PostMapping
    public ResponseEntity<OptionSubscriptionResponse> create(@Valid @RequestBody OptionSubscriptionCreateRequest req) {
        OptionSubscriptionResponse created = optionSubscriptionService.create(req);
        return ResponseEntity.created(URI.create("/api/option-subscriptions/" + created.subscriptionId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<OptionSubscriptionResponse>> getAll() {
        return ResponseEntity.ok(optionSubscriptionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OptionSubscriptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(optionSubscriptionService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OptionSubscriptionResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(optionSubscriptionService.getByUser(userId));
    }

    @GetMapping("/option/{optionId}")
    public ResponseEntity<List<OptionSubscriptionResponse>> getByOption(@PathVariable Long optionId) {
        return ResponseEntity.ok(optionSubscriptionService.getByOption(optionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OptionSubscriptionResponse> update(@PathVariable Long id,
                                                              @RequestBody OptionSubscriptionUpdateRequest req) {
        return ResponseEntity.ok(optionSubscriptionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        optionSubscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

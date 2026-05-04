package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.PurchaseOption.*;
import tn.esprit.pi_back.services.PurchaseOptionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/purchase-options")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PurchaseOptionController {

    private final PurchaseOptionService purchaseOptionService;

    @PostMapping
    public ResponseEntity<PurchaseOptionResponse> create(@Valid @RequestBody PurchaseOptionCreateRequest req) {
        PurchaseOptionResponse created = purchaseOptionService.create(req);
        return ResponseEntity.created(URI.create("/api/purchase-options/" + created.optionId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOptionResponse>> getAll() {
        return ResponseEntity.ok(purchaseOptionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOptionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOptionService.getById(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<PurchaseOptionResponse>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(purchaseOptionService.getByProject(projectId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOptionResponse> update(@PathVariable Long id,
                                                         @RequestBody PurchaseOptionUpdateRequest req) {
        return ResponseEntity.ok(purchaseOptionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        purchaseOptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

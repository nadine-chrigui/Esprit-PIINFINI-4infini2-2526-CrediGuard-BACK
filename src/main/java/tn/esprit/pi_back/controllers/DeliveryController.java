package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.delivery.*;
import tn.esprit.pi_back.entities.enums.DeliveryStatus;
import tn.esprit.pi_back.services.DeliveryService;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody DeliveryCreateRequest req) {
        return ResponseEntity.ok(deliveryService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody DeliveryUpdateRequest req) {
        return ResponseEntity.ok(deliveryService.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> byOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<DeliveryResponse>> mine() {
        return ResponseEntity.ok(deliveryService.getMine());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveriesAdmin(
            @RequestParam(required = false) DeliveryStatus status
    ) {
        return ResponseEntity.ok(deliveryService.getAllDeliveries(status));
    }

    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryUpdateRequest req
    ) {
        return ResponseEntity.ok(deliveryService.updateAdmin(id, req));
    }
}

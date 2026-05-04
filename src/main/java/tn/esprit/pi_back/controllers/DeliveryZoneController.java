package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.deliveryzone.*;
import tn.esprit.pi_back.services.DeliveryZoneService;

import java.util.List;

@RestController
@RequestMapping("/delivery-zones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeliveryZoneController {

    private final DeliveryZoneService deliveryZoneService;

    @GetMapping("/active")
    public ResponseEntity<List<DeliveryZoneResponse>> activeZones() {
        return ResponseEntity.ok(deliveryZoneService.getActive());
    }

    @PostMapping("/check")
    public ResponseEntity<DeliveryZoneCheckResponse> check(@Valid @RequestBody DeliveryZoneCheckRequest req) {
        return ResponseEntity.ok(deliveryZoneService.checkPoint(req.latitude(), req.longitude()));
    }

    @PostMapping("/check-address")
    public ResponseEntity<DeliveryFeeCheckResponse> checkAddress(@Valid @RequestBody DeliveryFeeCheckRequest req) {
        return ResponseEntity.ok(deliveryZoneService.checkAddress(req));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryZoneResponse>> all() {
        return ResponseEntity.ok(deliveryZoneService.getAll());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryZoneResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryZoneService.getById(id));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryZoneResponse> create(@Valid @RequestBody DeliveryZoneCreateRequest req) {
        return ResponseEntity.ok(deliveryZoneService.create(req));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryZoneResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryZoneUpdateRequest req
    ) {
        return ResponseEntity.ok(deliveryZoneService.update(id, req));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryZoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

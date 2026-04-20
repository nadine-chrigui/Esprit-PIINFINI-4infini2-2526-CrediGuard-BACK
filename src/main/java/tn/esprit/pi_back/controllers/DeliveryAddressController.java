package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.delivery.*;
import tn.esprit.pi_back.services.DeliveryAddressService;

import java.util.List;

@RestController
@RequestMapping("/delivery-addresses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    @PostMapping
    public ResponseEntity<DeliveryAddressResponse> create(@Valid @RequestBody DeliveryAddressCreateRequest req) {
        return ResponseEntity.ok(deliveryAddressService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliveryAddressResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody DeliveryAddressUpdateRequest req) {
        return ResponseEntity.ok(deliveryAddressService.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryAddressResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryAddressService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<DeliveryAddressResponse>> getAll() {
        return ResponseEntity.ok(deliveryAddressService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryAddressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
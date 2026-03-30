package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.order.*;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.services.OrderService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest req) {
        return ResponseEntity.ok(orderService.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMine() {
        return ResponseEntity.ok(orderService.getMine());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @RequestBody OrderUpdateRequest req) {
        return ResponseEntity.ok(orderService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/admin")
    public ResponseEntity<Page<OrderAdminResponse>> getAllAdmin(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(orderService.getAllAdmin(status, dateFrom, dateTo, pageable));
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<OrderResponse> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getAdminById(id));
    }

    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<OrderResponse> updateStatusAdmin(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(orderService.updateStatusAdmin(id, req));
    }
}
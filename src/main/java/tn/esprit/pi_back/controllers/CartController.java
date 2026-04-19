package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.cart.*;
import tn.esprit.pi_back.services.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping("/me")
    public ResponseEntity<CartResponse> me() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddItemRequest req) {
        return ResponseEntity.ok(cartService.addItem(req));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long itemId, @Valid @RequestBody UpdateItemRequest req) {
        return ResponseEntity.ok(cartService.updateItem(itemId, req));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clear() {
        return ResponseEntity.ok(cartService.clear());
    }
}
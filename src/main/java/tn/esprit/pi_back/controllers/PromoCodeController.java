package tn.esprit.pi_back.controllers;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.promocode.*;
import tn.esprit.pi_back.services.PromoCodeService;

import java.util.List;
import jakarta.annotation.PostConstruct;



@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    public ResponseEntity<PromoCodeResponse> create(@Valid @RequestBody PromoCodeCreateRequest req) {
        return ResponseEntity.ok(promoCodeService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody PromoCodeUpdateRequest req) {
        return ResponseEntity.ok(promoCodeService.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PromoCodeResponse>> getAll() {
        return ResponseEntity.ok(promoCodeService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promoCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // validate/apply without creating an order
    @PostMapping("/validate")
    public ResponseEntity<PromoCodeValidateResponse> validate(@Valid @RequestBody PromoCodeValidateRequest req) {
        return ResponseEntity.ok(promoCodeService.validateAndCompute(req));
    }



    @PostConstruct
    public void init() {
        System.out.println("✅ PromoCodeController LOADED");
    }
}
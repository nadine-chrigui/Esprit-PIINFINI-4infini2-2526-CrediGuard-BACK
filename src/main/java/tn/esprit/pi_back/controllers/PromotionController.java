package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.promotion.PromotionCreateRequest;
import tn.esprit.pi_back.dto.promotion.PromotionResponse;
import tn.esprit.pi_back.services.PromotionService;

import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public PromotionResponse create(@Valid @RequestBody PromotionCreateRequest request) {
        return promotionService.create(request);
    }

    @PutMapping("/{id}")
    public PromotionResponse update(@PathVariable Long id,
                                    @Valid @RequestBody PromotionCreateRequest request) {
        return promotionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        promotionService.delete(id);
    }

    @GetMapping("/{id}")
    public PromotionResponse getById(@PathVariable Long id) {
        return promotionService.getById(id);
    }

    @GetMapping
    public List<PromotionResponse> getAll() {
        return promotionService.getAll();
    }

    @GetMapping("/active")
    public List<PromotionResponse> getActive() {
        return promotionService.getActive();
    }
}
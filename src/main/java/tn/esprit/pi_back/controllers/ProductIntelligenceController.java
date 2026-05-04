package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceHistoryResponse;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceModelInfoResponse;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceResponse;
import tn.esprit.pi_back.services.ProductIntelligenceService;

import java.util.List;

@RestController
@RequestMapping("/product-intelligence")
@RequiredArgsConstructor
public class ProductIntelligenceController {

    private final ProductIntelligenceService productIntelligenceService;

    @GetMapping("/admin")
    public ResponseEntity<List<ProductIntelligenceResponse>> getAllAdmin() {
        return ResponseEntity.ok(productIntelligenceService.getAll());
    }

    @GetMapping("/admin/model-info")
    public ResponseEntity<ProductIntelligenceModelInfoResponse> getModelInfo() {
        return ResponseEntity.ok(productIntelligenceService.getModelInfo());
    }

    @GetMapping("/admin/product/{productId}")
    public ResponseEntity<ProductIntelligenceResponse> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productIntelligenceService.getByProductId(productId));
    }

    @GetMapping("/admin/product/{productId}/history")
    public ResponseEntity<List<ProductIntelligenceHistoryResponse>> getHistoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productIntelligenceService.getHistoryByProductId(productId));
    }

    @PostMapping("/admin/analyze")
    public ResponseEntity<List<ProductIntelligenceResponse>> analyzeAll() {
        return ResponseEntity.ok(productIntelligenceService.analyzeAll());
    }

    @PostMapping("/admin/products/{productId}/analyze")
    public ResponseEntity<ProductIntelligenceResponse> analyzeProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productIntelligenceService.analyzeProduct(productId));
    }
}

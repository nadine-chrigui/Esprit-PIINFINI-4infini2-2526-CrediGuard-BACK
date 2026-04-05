package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi_back.dto.product.ProductCreateRequest;
import tn.esprit.pi_back.dto.product.ProductResponse;
import tn.esprit.pi_back.dto.product.ProductUpdateRequest;
import tn.esprit.pi_back.services.ProductService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    /**
     * Public list of products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    /**
     * Public product detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * Products of current logged seller
     * Allowed only for BENEFICIARY and PARTNER
     */
    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ADMIN','BENEFICIARY','PARTNER')")
    public ResponseEntity<List<ProductResponse>> getMine() {
        return ResponseEntity.ok(productService.getMine());
    }

    /**
     * Create product by current logged seller
     * Allowed only for BENEFICIARY and PARTNER
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BENEFICIARY','PARTNER')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        ProductResponse created = productService.create(req);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.id()))
                .body(created);
    }

    /**
     * Update own product
     * Allowed only for BENEFICIARY and PARTNER
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BENEFICIARY','PARTNER')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest req
    ) {
        return ResponseEntity.ok(productService.update(id, req));
    }

    /**
     * Delete own product
     * Allowed only for BENEFICIARY and PARTNER
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BENEFICIARY','PARTNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload product image
     * Allowed only for BENEFICIARY and PARTNER
     */
    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN','BENEFICIARY','PARTNER')")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String fileName = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path targetPath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/uploads/" + fileName;
        return ResponseEntity.ok(fileUrl);
    }

    /**
     * Public products by seller id
     * Useful for seller public profile / storefront
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponse>> getBySellerId(@PathVariable Long sellerId) {
        return ResponseEntity.ok(productService.getBySellerId(sellerId));
    }
}
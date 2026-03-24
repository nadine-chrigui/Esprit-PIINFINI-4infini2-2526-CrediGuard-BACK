package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.product.*;
import tn.esprit.pi_back.entities.Category;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.CategoryRepository;
import tn.esprit.pi_back.repositories.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService; // doit exposer getOrCreateCurrentUser()

    @Override
    public ProductResponse create(ProductCreateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));

        Product p = new Product();
        p.setSeller(me);
        p.setCategory(category);

        p.setName(req.name().trim());
        p.setDescription(req.description());

        p.setBasePrice(req.basePrice());
        p.setCurrentPrice(req.basePrice()); // safe
        p.setDynamicPricingEnabled(req.dynamicPricingEnabled() != null && req.dynamicPricingEnabled());
        p.setPricingStrategy(req.pricingStrategy());

        p.setSaleType(req.saleType());
        p.setStockQuantity(req.stockQuantity());
        p.setPreorderQuota(req.preorderQuota());
        p.setPaymentMode(req.paymentMode());
        p.setDepositPercentage(req.depositPercentage());

        p.setExpressDeliveryAvailable(req.expressDeliveryAvailable() != null && req.expressDeliveryAvailable());
        p.setExpressDeliveryFee(req.expressDeliveryFee());

        p.setPreorderStartDate(req.preorderStartDate());
        p.setPreorderEndDate(req.preorderEndDate());
        p.setExpectedReleaseDate(req.expectedReleaseDate());

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product p = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMine() {
        User me = userService.getOrCreateCurrentUser();
        return productRepository.findBySellerIdAndActiveTrue(me.getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public ProductResponse update(Long id, ProductUpdateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Product p = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        if (!p.getSeller().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: you are not the owner of this product.");
        }

        if (req.categoryId() != null) {
            Category category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));
            p.setCategory(category);
        }

        if (req.name() != null) p.setName(req.name().trim());
        if (req.description() != null) p.setDescription(req.description());

        if (req.basePrice() != null) p.setBasePrice(req.basePrice());
        if (req.currentPrice() != null) p.setCurrentPrice(req.currentPrice());

        if (req.dynamicPricingEnabled() != null) p.setDynamicPricingEnabled(req.dynamicPricingEnabled());
        if (req.pricingStrategy() != null) p.setPricingStrategy(req.pricingStrategy());

        if (req.saleType() != null) p.setSaleType(req.saleType());

        if (req.stockQuantity() != null) p.setStockQuantity(req.stockQuantity());
        if (req.preorderQuota() != null) p.setPreorderQuota(req.preorderQuota());

        if (req.paymentMode() != null) p.setPaymentMode(req.paymentMode());
        if (req.depositPercentage() != null) p.setDepositPercentage(req.depositPercentage());

        if (req.expressDeliveryAvailable() != null) p.setExpressDeliveryAvailable(req.expressDeliveryAvailable());
        if (req.expressDeliveryFee() != null) p.setExpressDeliveryFee(req.expressDeliveryFee());

        if (req.preorderStartDate() != null) p.setPreorderStartDate(req.preorderStartDate());
        if (req.preorderEndDate() != null) p.setPreorderEndDate(req.preorderEndDate());
        if (req.expectedReleaseDate() != null) p.setExpectedReleaseDate(req.expectedReleaseDate());

        if (req.active() != null) p.setActive(req.active());

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Product p = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        if (!p.getSeller().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: you are not the owner of this product.");
        }


        p.setActive(false);
        productRepository.save(p);
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getSeller() != null ? p.getSeller().getId() : null,
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getName(),
                p.getDescription(),
                p.getBasePrice(),
                p.getCurrentPrice(),
                p.isDynamicPricingEnabled(),
                p.getPricingStrategy(),
                p.getSaleType(),
                p.getStockQuantity(),
                p.getPreorderQuota(),
                p.getPreorderCount(),
                p.getPaymentMode(),
                p.getDepositPercentage(),
                p.isExpressDeliveryAvailable(),
                p.getExpressDeliveryFee(),
                p.getPreorderStartDate(),
                p.getPreorderEndDate(),
                p.getExpectedReleaseDate(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
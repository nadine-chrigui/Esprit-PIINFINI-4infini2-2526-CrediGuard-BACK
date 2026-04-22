package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.product.ProductCreateRequest;
import tn.esprit.pi_back.dto.product.ProductResponse;
import tn.esprit.pi_back.dto.product.ProductUpdateRequest;
import tn.esprit.pi_back.dto.promotion.ProductPriceView;
import tn.esprit.pi_back.entities.Category;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.mappers.ProductMapper;
import tn.esprit.pi_back.repositories.CategoryRepository;
import tn.esprit.pi_back.repositories.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final ProductMapper productMapper;
    private final PromotionService promotionService;

    @Override
    public ProductResponse create(ProductCreateRequest req) {
        User me = getAuthorizedSeller();

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));

        Product product = productMapper.toEntity(req, me, category);
        Product saved = productRepository.save(product);

        return enrich(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::enrich)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        return enrich(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMine() {
        User me = getAuthorizedSeller();

        return productRepository.findBySellerIdAndActiveTrue(me.getId())
                .stream()
                .map(this::enrich)
                .toList();
    }

    @Override
    public ProductResponse update(Long id, ProductUpdateRequest req) {
        User me = getAuthorizedSeller();

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        validateOwnership(product, me);

        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));
        }

        productMapper.updateEntity(product, req, category);

        Product saved = productRepository.save(product);
        return enrich(saved);
    }

    @Override
    public void delete(Long id) {
        User me = getAuthorizedSeller();

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        validateOwnership(product, me);

        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getBySellerId(Long sellerId) {
        return productRepository.findBySellerIdAndActiveTrue(sellerId)
                .stream()
                .map(this::enrich)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllAdmin(Boolean active) {
        List<Product> products;

        if (active == null) {
            products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            products = productRepository.findByActiveOrderByCreatedAtDesc(active);
        }

        return products.stream()
                .map(this::enrich)
                .toList();
    }

    @Override
    public ProductResponse updateActiveAdmin(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        product.setActive(active);
        Product saved = productRepository.save(product);
        return enrich(saved);
    }

    private ProductResponse enrich(Product product) {
        ProductResponse base = productMapper.toResponse(product);
        ProductPriceView priceView = promotionService.calculateProductPrice(product, null);

        return new ProductResponse(
                base.id(),
                base.sellerId(),
                base.sellerName(),
                base.categoryId(),
                base.categoryName(),
                base.name(),
                base.description(),
                base.basePrice(),
                base.currentPrice(),
                base.dynamicPricingEnabled(),
                base.pricingStrategy(),
                base.saleType(),
                base.stockQuantity(),
                base.preorderQuota(),
                base.preorderCount(),
                base.paymentMode(),
                base.depositPercentage(),
                base.expressDeliveryAvailable(),
                base.expressDeliveryFee(),
                base.preorderStartDate(),
                base.preorderEndDate(),
                base.expectedReleaseDate(),
                base.active(),
                base.createdAt(),
                base.updatedAt(),
                base.imageUrl(),
                priceView.originalPrice(),
                priceView.finalPrice(),
                priceView.discountAmount(),
                priceView.promotionApplied(),
                priceView.promotionName()
        );
    }

    private User getAuthorizedSeller() {
        User me = userService.getCurrentUserOrThrow();

        if (me.getUserType() != UserType.ADMIN
                && me.getUserType() != UserType.BENEFICIARY
                && me.getUserType() != UserType.PARTNER) {
            throw new SecurityException("Only ADMIN, BENEFICIARY or PARTNER can manage products.");
        }

        return me;
    }

    private void validateOwnership(Product product, User me) {
        if (product.getSeller() == null || product.getSeller().getId() == null) {
            throw new SecurityException("This product has no valid seller.");
        }

        if (me.getUserType() == UserType.ADMIN) {
            return;
        }

        if (!product.getSeller().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: you are not the owner of this product.");
        }
    }
}

package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.product.ProductCreateRequest;
import tn.esprit.pi_back.dto.product.ProductResponse;
import tn.esprit.pi_back.dto.product.ProductUpdateRequest;
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

    @Override
    public ProductResponse create(ProductCreateRequest req) {
        User me = getAuthorizedSeller();

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.categoryId()));

        Product product = productMapper.toEntity(req, me, category);
        Product saved = productRepository.save(product);

        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getMine() {
        User me = getAuthorizedSeller();

        return productRepository.findBySellerIdAndActiveTrue(me.getId())
                .stream()
                .map(productMapper::toResponse)
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
        return productMapper.toResponse(saved);
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
                .map(productMapper::toResponse)
                .toList();
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
package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.product.ProductCreateRequest;
import tn.esprit.pi_back.dto.product.ProductResponse;
import tn.esprit.pi_back.dto.product.ProductUpdateRequest;
import tn.esprit.pi_back.entities.Category;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.User;

@Component
public class ProductMapper {

    public Product toEntity(ProductCreateRequest req, User seller, Category category) {
        Product product = new Product();

        product.setSeller(seller);
        product.setCategory(category);

        product.setName(req.name().trim());
        product.setDescription(req.description());

        product.setBasePrice(req.basePrice());
        product.setCurrentPrice(req.basePrice());

        product.setDynamicPricingEnabled(Boolean.TRUE.equals(req.dynamicPricingEnabled()));
        product.setPricingStrategy(req.pricingStrategy());

        product.setSaleType(req.saleType());
        product.setStockQuantity(req.stockQuantity());
        product.setPreorderQuota(req.preorderQuota());

        product.setPaymentMode(req.paymentMode());
        product.setImageUrl(req.imageUrl());
        product.setDepositPercentage(req.depositPercentage());

        product.setExpressDeliveryAvailable(Boolean.TRUE.equals(req.expressDeliveryAvailable()));
        product.setExpressDeliveryFee(req.expressDeliveryFee());

        product.setPreorderStartDate(req.preorderStartDate());
        product.setPreorderEndDate(req.preorderEndDate());
        product.setExpectedReleaseDate(req.expectedReleaseDate());

        return product;
    }

    public void updateEntity(Product product, ProductUpdateRequest req, Category category) {
        if (category != null) {
            product.setCategory(category);
        }

        if (req.name() != null) product.setName(req.name().trim());
        if (req.description() != null) product.setDescription(req.description());

        if (req.basePrice() != null) product.setBasePrice(req.basePrice());
        if (req.currentPrice() != null) product.setCurrentPrice(req.currentPrice());

        if (req.dynamicPricingEnabled() != null) {
            product.setDynamicPricingEnabled(req.dynamicPricingEnabled());
        }

        if (req.pricingStrategy() != null) product.setPricingStrategy(req.pricingStrategy());
        if (req.saleType() != null) product.setSaleType(req.saleType());

        if (req.imageUrl() != null) product.setImageUrl(req.imageUrl());

        if (req.stockQuantity() != null) product.setStockQuantity(req.stockQuantity());
        if (req.preorderQuota() != null) product.setPreorderQuota(req.preorderQuota());

        if (req.paymentMode() != null) product.setPaymentMode(req.paymentMode());
        if (req.depositPercentage() != null) product.setDepositPercentage(req.depositPercentage());

        if (req.expressDeliveryAvailable() != null) {
            product.setExpressDeliveryAvailable(req.expressDeliveryAvailable());
        }

        if (req.expressDeliveryFee() != null) product.setExpressDeliveryFee(req.expressDeliveryFee());

        if (req.preorderStartDate() != null) product.setPreorderStartDate(req.preorderStartDate());
        if (req.preorderEndDate() != null) product.setPreorderEndDate(req.preorderEndDate());
        if (req.expectedReleaseDate() != null) product.setExpectedReleaseDate(req.expectedReleaseDate());

        if (req.active() != null) product.setActive(req.active());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSeller() != null ? product.getSeller().getId() : null,
                product.getSeller() != null ? product.getSeller().getFullName() : null,
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                product.getCurrentPrice(),
                product.isDynamicPricingEnabled(),
                product.getPricingStrategy(),
                product.getSaleType(),
                product.getStockQuantity(),
                product.getPreorderQuota(),
                product.getPreorderCount(),
                product.getPaymentMode(),
                product.getDepositPercentage(),
                product.isExpressDeliveryAvailable(),
                product.getExpressDeliveryFee(),
                product.getPreorderStartDate(),
                product.getPreorderEndDate(),
                product.getExpectedReleaseDate(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getImageUrl()
        );
    }
}
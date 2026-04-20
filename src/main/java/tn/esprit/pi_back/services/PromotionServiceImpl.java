package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.promotion.ProductPriceView;
import tn.esprit.pi_back.dto.promotion.PromotionCreateRequest;
import tn.esprit.pi_back.dto.promotion.PromotionResponse;
import tn.esprit.pi_back.entities.CalendarEvent;
import tn.esprit.pi_back.entities.Category;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.Promotion;
import tn.esprit.pi_back.entities.enums.DiscountType;
import tn.esprit.pi_back.entities.enums.PromotionTargetType;
import tn.esprit.pi_back.mappers.PromotionMapper;
import tn.esprit.pi_back.repositories.CalendarEventRepository;
import tn.esprit.pi_back.repositories.CategoryRepository;
import tn.esprit.pi_back.repositories.ProductRepository;
import tn.esprit.pi_back.repositories.PromotionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CalendarEventRepository calendarEventRepository;

    @Override
    public PromotionResponse create(PromotionCreateRequest request) {
        validateRequest(request);

        Promotion promotion = new Promotion();
        mapToEntity(request, promotion);

        return PromotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Override
    public PromotionResponse update(Long id, PromotionCreateRequest request) {
        validateRequest(request);

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + id));

        mapToEntity(request, promotion);

        return PromotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Override
    public void delete(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + id));
        promotionRepository.delete(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found: " + id));
        return PromotionMapper.toResponse(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getAll() {
        return promotionRepository.findAll()
                .stream()
                .map(PromotionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> getActive() {
        return promotionRepository.findAllEnabled()
                .stream()
                .filter(this::isPromotionCurrentlyActive)
                .map(PromotionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPriceView calculateProductPrice(Product product, Double cartAmount) {
        double originalPrice = product.getCurrentPrice() != null
                ? product.getCurrentPrice()
                : product.getBasePrice();

        List<Promotion> candidates = promotionRepository.findAllEnabled()
                .stream()
                .filter(this::isPromotionCurrentlyActive)
                .filter(promotion -> isApplicable(promotion, product, cartAmount))
                .sorted((a, b) -> Integer.compare(
                        b.getPriority() != null ? b.getPriority() : 0,
                        a.getPriority() != null ? a.getPriority() : 0
                ))
                .toList();

        if (candidates.isEmpty()) {
            return new ProductPriceView(
                    product.getId(),
                    originalPrice,
                    originalPrice,
                    0.0,
                    false,
                    null
            );
        }

        Promotion best = candidates.get(0);
        double discount = calculateDiscount(best, originalPrice);
        double finalPrice = Math.max(0.0, originalPrice - discount);

        return new ProductPriceView(
                product.getId(),
                originalPrice,
                finalPrice,
                discount,
                true,
                best.getName()
        );
    }

    private boolean isPromotionCurrentlyActive(Promotion promotion) {
        if (!Boolean.TRUE.equals(promotion.getActive())) {
            return false;
        }

        if (!Boolean.TRUE.equals(promotion.getAutoApply())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start;
        LocalDateTime end;

        if (promotion.getCalendarEvent() != null) {
            CalendarEvent event = promotion.getCalendarEvent();

            if (!Boolean.TRUE.equals(event.getActive())) {
                return false;
            }

            start = event.getStartDate();
            end = event.getEndDate();
        } else {
            start = promotion.getStartDate();
            end = promotion.getEndDate();
        }

        if (start != null && now.isBefore(start)) {
            return false;
        }

        if (end != null && now.isAfter(end)) {
            return false;
        }

        return true;
    }

    private boolean isApplicable(Promotion promotion, Product product, Double cartAmount) {
        if (promotion.getMinOrderAmount() != null && cartAmount != null
                && cartAmount < promotion.getMinOrderAmount()) {
            return false;
        }

        return switch (promotion.getTargetType()) {
            case ALL_PRODUCTS -> true;
            case CATEGORY -> promotion.getCategory() != null
                    && product.getCategory() != null
                    && promotion.getCategory().getId().equals(product.getCategory().getId());
            case PRODUCT -> promotion.getProduct() != null
                    && promotion.getProduct().getId().equals(product.getId());
        };
    }

    private double calculateDiscount(Promotion promotion, double price) {
        double discount;

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = price * promotion.getDiscountValue() / 100.0;
        } else {
            discount = promotion.getDiscountValue();
        }

        if (promotion.getMaxDiscountAmount() != null) {
            discount = Math.min(discount, promotion.getMaxDiscountAmount());
        }

        return Math.min(discount, price);
    }

    private void validateRequest(PromotionCreateRequest request) {
        if (request.calendarEventId() == null) {
            if (request.startDate() != null && request.endDate() != null
                    && request.endDate().isBefore(request.startDate())) {
                throw new IllegalArgumentException("endDate must be after startDate");
            }
        }

        if (request.discountType() == DiscountType.PERCENTAGE
                && request.discountValue() != null
                && request.discountValue() > 100) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100");
        }

        switch (request.targetType()) {
            case ALL_PRODUCTS -> {
                if (request.categoryId() != null || request.productId() != null) {
                    throw new IllegalArgumentException("ALL_PRODUCTS must not have categoryId or productId");
                }
            }
            case CATEGORY -> {
                if (request.categoryId() == null) {
                    throw new IllegalArgumentException("CATEGORY promotion requires categoryId");
                }
            }
            case PRODUCT -> {
                if (request.productId() == null) {
                    throw new IllegalArgumentException("PRODUCT promotion requires productId");
                }
            }
        }
    }

    private void mapToEntity(PromotionCreateRequest request, Promotion promotion) {
        promotion.setName(request.name());
        promotion.setDescription(request.description());
        promotion.setPromotionType(request.promotionType());
        promotion.setDiscountType(request.discountType());
        promotion.setTargetType(request.targetType());
        promotion.setDiscountValue(request.discountValue());
        promotion.setMinOrderAmount(request.minOrderAmount());
        promotion.setMaxDiscountAmount(request.maxDiscountAmount());
        promotion.setActive(request.active() != null ? request.active() : true);
        promotion.setPriority(request.priority() != null ? request.priority() : 0);
        promotion.setAutoApply(request.autoApply() != null ? request.autoApply() : true);
        promotion.setStackable(request.stackable() != null ? request.stackable() : false);
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());

        promotion.setCategory(null);
        promotion.setProduct(null);
        promotion.setCalendarEvent(null);

        if (request.targetType() == PromotionTargetType.CATEGORY) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));
            promotion.setCategory(category);
        }

        if (request.targetType() == PromotionTargetType.PRODUCT) {
            Product product = productRepository.findById(request.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.productId()));
            promotion.setProduct(product);
        }

        if (request.calendarEventId() != null) {
            CalendarEvent event = calendarEventRepository.findById(request.calendarEventId())
                    .orElseThrow(() -> new IllegalArgumentException("Calendar event not found: " + request.calendarEventId()));
            promotion.setCalendarEvent(event);

            // si liée à un event, la promo hérite de ses dates
            promotion.setStartDate(null);
            promotion.setEndDate(null);
        }
    }
}
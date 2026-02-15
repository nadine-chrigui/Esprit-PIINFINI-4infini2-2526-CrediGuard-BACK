package tn.esprit.pi_back.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.promocode.*;
import tn.esprit.pi_back.entities.PromoCode;
import tn.esprit.pi_back.entities.enums.DiscountType;
import tn.esprit.pi_back.repositories.PromoCodeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    @Override
    public PromoCodeResponse create(PromoCodeCreateRequest req) {
        String code = req.code().trim().toUpperCase();

        if (promoCodeRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Promo code already exists: " + code);
        }

        PromoCode p = new PromoCode();
        p.setCode(code);
        p.setDiscountType(req.discountType());
        p.setDiscountValue(req.discountValue());

        p.setActive(req.active() != null ? req.active() : true);
        p.setMaxUses(req.maxUses());
        p.setUsedCount(0);

        p.setMinOrderAmount(req.minOrderAmount());
        p.setMaxDiscountAmount(req.maxDiscountAmount());

        p.setStartAt(req.startAt());
        p.setEndAt(req.endAt());

        PromoCode saved = promoCodeRepository.save(p);
        return toResponse(saved);
    }

    @Override
    public PromoCodeResponse update(Long id, PromoCodeUpdateRequest req) {
        PromoCode p = promoCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PromoCode not found: " + id));

        if (req.code() != null) {
            String newCode = req.code().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(p.getCode()) && promoCodeRepository.existsByCodeIgnoreCase(newCode)) {
                throw new IllegalArgumentException("Promo code already exists: " + newCode);
            }
            p.setCode(newCode);
        }

        if (req.discountType() != null) p.setDiscountType(req.discountType());
        if (req.discountValue() != null) p.setDiscountValue(req.discountValue());

        if (req.active() != null) p.setActive(req.active());
        if (req.maxUses() != null) p.setMaxUses(req.maxUses());

        if (req.minOrderAmount() != null) p.setMinOrderAmount(req.minOrderAmount());
        if (req.maxDiscountAmount() != null) p.setMaxDiscountAmount(req.maxDiscountAmount());

        if (req.startAt() != null) p.setStartAt(req.startAt());
        if (req.endAt() != null) p.setEndAt(req.endAt());

        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponse getById(Long id) {
        PromoCode p = promoCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PromoCode not found: " + id));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getAll() {
        return promoCodeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public void delete(Long id) {
        // soft delete recommandé: active=false
        PromoCode p = promoCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PromoCode not found: " + id));
        p.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeValidateResponse validateAndCompute(PromoCodeValidateRequest req) {
        PromoCode p = promoCodeRepository.findByCodeIgnoreCase(req.code().trim())
                .orElse(null);

        if (p == null) {
            return new PromoCodeValidateResponse(false, "Promo code not found", 0.0, req.orderAmount(), null);
        }

        String msg = validatePromo(p, req.orderAmount());
        if (msg != null) {
            return new PromoCodeValidateResponse(false, msg, 0.0, req.orderAmount(), p.getId());
        }

        double discount = computeDiscount(p, req.orderAmount());
        double finalAmount = Math.max(0.0, req.orderAmount() - discount);

        return new PromoCodeValidateResponse(true, "OK", discount, finalAmount, p.getId());
    }

    @Override
    public void incrementUse(Long promoCodeId) {
        PromoCode p = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new IllegalArgumentException("PromoCode not found: " + promoCodeId));

        if (p.getUsedCount() == null) p.setUsedCount(0);
        p.setUsedCount(p.getUsedCount() + 1);
    }

    private String validatePromo(PromoCode p, double orderAmount) {
        if (Boolean.FALSE.equals(p.getActive())) return "Promo code is inactive";

        if (p.getMaxUses() != null && p.getUsedCount() != null && p.getUsedCount() >= p.getMaxUses()) {
            return "Promo code has reached max uses";
        }

        if (p.getMinOrderAmount() != null && orderAmount < p.getMinOrderAmount()) {
            return "Order amount is below minimum required";
        }

        LocalDateTime now = LocalDateTime.now();
        if (p.getStartAt() != null && now.isBefore(p.getStartAt())) return "Promo code not started yet";
        if (p.getEndAt() != null && now.isAfter(p.getEndAt())) return "Promo code expired";

        return null; // OK
    }

    private double computeDiscount(PromoCode p, double orderAmount) {
        double discount;

        if (p.getDiscountType() == DiscountType.PERCENTAGE) {
            // discountValue ex: 10 => 10%
            discount = orderAmount * (p.getDiscountValue() / 100.0);
            if (p.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, p.getMaxDiscountAmount());
            }
        } else { // FIXED
            discount = p.getDiscountValue();
        }

        // discount ne dépasse pas le montant
        return Math.min(discount, orderAmount);
    }

    private PromoCodeResponse toResponse(PromoCode p) {
        return new PromoCodeResponse(
                p.getId(),
                p.getCode(),
                p.getDiscountType(),
                p.getDiscountValue(),
                p.getActive(),
                p.getMaxUses(),
                p.getUsedCount(),
                p.getMinOrderAmount(),
                p.getMaxDiscountAmount(),
                p.getStartAt(),
                p.getEndAt(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
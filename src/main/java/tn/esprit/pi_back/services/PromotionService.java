package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.promotion.ProductPriceView;
import tn.esprit.pi_back.dto.promotion.PromotionCreateRequest;
import tn.esprit.pi_back.dto.promotion.PromotionResponse;
import tn.esprit.pi_back.entities.Product;

import java.util.List;

public interface PromotionService {
    PromotionResponse create(PromotionCreateRequest request);
    PromotionResponse update(Long id, PromotionCreateRequest request);
    void delete(Long id);
    PromotionResponse getById(Long id);
    List<PromotionResponse> getAll();
    List<PromotionResponse> getActive();

    ProductPriceView calculateProductPrice(Product product, Double cartAmount);
}
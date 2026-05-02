package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceResponse;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceHistoryResponse;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceModelInfoResponse;

import java.util.List;

public interface ProductIntelligenceService {
    List<ProductIntelligenceResponse> getAll();

    ProductIntelligenceResponse getByProductId(Long productId);

    List<ProductIntelligenceHistoryResponse> getHistoryByProductId(Long productId);

    ProductIntelligenceResponse analyzeProduct(Long productId);

    List<ProductIntelligenceResponse> analyzeAll();

    ProductIntelligenceModelInfoResponse getModelInfo();
}

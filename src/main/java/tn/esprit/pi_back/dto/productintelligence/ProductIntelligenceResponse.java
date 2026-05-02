package tn.esprit.pi_back.dto.productintelligence;

import tn.esprit.pi_back.entities.enums.ProductPerformanceLabel;
import tn.esprit.pi_back.entities.enums.ProductRiskLevel;
import tn.esprit.pi_back.entities.enums.ProductSuggestedAction;

import java.time.LocalDateTime;
import java.util.List;

public record ProductIntelligenceResponse(
        Long id,
        Long productId,
        String productName,
        String categoryName,
        Integer currentStock,
        Integer salesLast7Days,
        Integer salesLast30Days,
        ProductRiskLevel riskLevel,
        Integer daysToStockout,
        Integer recommendedRestock,
        Integer performanceScore,
        ProductPerformanceLabel performanceLabel,
        ProductSuggestedAction suggestedAction,
        List<String> reasons,
        Integer riskConfidence,
        Integer actionConfidence,
        String mlDecision,
        List<String> mainDrivers,
        String businessRecommendation,
        String modelType,
        LocalDateTime analyzedAt
) {
}

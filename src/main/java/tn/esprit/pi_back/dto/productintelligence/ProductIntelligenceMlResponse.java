package tn.esprit.pi_back.dto.productintelligence;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProductIntelligenceMlResponse(
        @JsonProperty("product_id")
        Long productId,

        @JsonProperty("risk_level")
        String riskLevel,

        @JsonProperty("days_to_stockout")
        Integer daysToStockout,

        @JsonProperty("recommended_restock")
        Integer recommendedRestock,

        @JsonProperty("performance_score")
        Integer performanceScore,

        @JsonProperty("performance_label")
        String performanceLabel,

        @JsonProperty("suggested_action")
        String suggestedAction,

        List<String> reasons,

        @JsonProperty("risk_confidence")
        Integer riskConfidence,

        @JsonProperty("action_confidence")
        Integer actionConfidence,

        @JsonProperty("ml_decision")
        String mlDecision,

        @JsonProperty("main_drivers")
        List<String> mainDrivers,

        @JsonProperty("business_recommendation")
        String businessRecommendation,

        @JsonProperty("model_type")
        String modelType
) {
}

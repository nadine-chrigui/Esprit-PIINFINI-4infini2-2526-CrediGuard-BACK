package tn.esprit.pi_back.dto.productintelligence;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductIntelligenceMlRequest(
        @JsonProperty("product_id")
        Long productId,

        Integer stock,

        @JsonProperty("sales_last_7_days")
        Integer salesLast7Days,

        @JsonProperty("sales_last_30_days")
        Integer salesLast30Days,

        Double price
) {
}

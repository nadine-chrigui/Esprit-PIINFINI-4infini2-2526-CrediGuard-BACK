package tn.esprit.pi_back.dto.Projection;

import java.time.LocalDateTime;

public record ProjectProjectionResponse(
        Long id,
        Long projectId,
        Long purchaseOptionId,
        Double totalInvestment,
        Double interestRate,
        Double optionPrice,
        Integer quantity,
        Integer soldQuantity,
        Integer remainingQuantity,
        Double estimatedSalesRate,
        Double growthRate,
        Integer durationYears,
        Double currentRevenue,
        Double futureRevenue,
        Double investorCost,
        Double netProfit,
        String summaryMessage,
        LocalDateTime createdAt
) {}

package tn.esprit.pi_back.dto.Projection;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProjectProjectionRequest(
        @NotNull Long projectId,
        @NotNull Long purchaseOptionId,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double estimatedSalesRate,
        @NotNull @DecimalMin("0.0") Double growthRate,
        @NotNull @Min(1) Integer durationYears
) {}

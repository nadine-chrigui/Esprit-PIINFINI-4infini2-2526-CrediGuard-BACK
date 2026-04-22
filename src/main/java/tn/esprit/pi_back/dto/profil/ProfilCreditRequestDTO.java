package tn.esprit.pi_back.dto.profil;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.enums.ProfilDefaultFlag;
import tn.esprit.pi_back.entities.enums.ProfilHomeOwnership;
import tn.esprit.pi_back.entities.enums.ProfilLoanIntent;

public record ProfilCreditRequestDTO(

        @NotNull(message = "personAge is required")
        @Min(value = 18, message = "personAge must be >= 18")
        @Max(value = 100, message = "personAge must be <= 100")
        Integer personAge,

        @NotNull(message = "personIncomeAnnual is required")
        @DecimalMin(value = "1000.0", message = "personIncomeAnnual must be >= 1000")
        @DecimalMax(value = "1000000.0", message = "personIncomeAnnual must be <= 1000000")
        Double personIncomeAnnual,

        @NotNull(message = "personHomeOwnership is required")
        ProfilHomeOwnership personHomeOwnership,

        @NotNull(message = "personEmploymentLength is required")
        @DecimalMin(value = "0.0", message = "personEmploymentLength must be >= 0")
        @DecimalMax(value = "60.0", message = "personEmploymentLength must be <= 60")
        Double personEmploymentLength,

        @NotNull(message = "previousDefaultOnFile is required")
        ProfilDefaultFlag previousDefaultOnFile,

        @NotNull(message = "creditHistoryLength is required")
        @Min(value = 1, message = "creditHistoryLength must be >= 1")
        @Max(value = 50, message = "creditHistoryLength must be <= 50")
        Integer creditHistoryLength,

        @NotNull(message = "loanIntent is required")
        ProfilLoanIntent loanIntent,

        @NotNull(message = "monthlyFixedCharges is required")
        @DecimalMin(value = "0.0", message = "monthlyFixedCharges must be >= 0")
        Double monthlyFixedCharges,

        @NotNull(message = "existingLoanMonthlyPayments is required")
        @DecimalMin(value = "0.0", message = "existingLoanMonthlyPayments must be >= 0")
        Double existingLoanMonthlyPayments,

        @NotNull(message = "outstandingOldDebt is required")
        @DecimalMin(value = "0.0", message = "outstandingOldDebt must be >= 0")
        Double outstandingOldDebt,

        @NotNull(message = "projectStartDelayMonths is required")
        @Min(value = 0, message = "projectStartDelayMonths must be >= 0")
        @Max(value = 12, message = "projectStartDelayMonths must be <= 12")
        Integer projectStartDelayMonths,

        @NotNull(message = "expectedMonthlyRevenueAfterStart is required")
        @DecimalMin(value = "0.0", message = "expectedMonthlyRevenueAfterStart must be >= 0")
        Double expectedMonthlyRevenueAfterStart,

        @NotNull(message = "hasExistingClients is required")
        Boolean hasExistingClients,

        @NotNull(message = "needsGracePeriod is required")
        Boolean needsGracePeriod
) {
}

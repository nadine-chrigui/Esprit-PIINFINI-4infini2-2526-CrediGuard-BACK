package tn.esprit.pi_back.dto.evaluation;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import tn.esprit.pi_back.entities.enums.ProfilLoanGrade;

public record EvaluationPredictionRequestDTO(

        ProfilLoanGrade loanGrade,

        @DecimalMin(value = "1.0", message = "interestRate must be >= 1")
        @DecimalMax(value = "30.0", message = "interestRate must be <= 30")
        Double interestRate,

        @Min(value = 100, message = "nSim must be >= 100")
        @Max(value = 2000, message = "nSim must be <= 2000")
        Integer nSim,

        @DecimalMin(value = "0.0", message = "noiseFactor must be >= 0")
        @DecimalMax(value = "0.50", message = "noiseFactor must be <= 0.50")
        Double noiseFactor
) {
}

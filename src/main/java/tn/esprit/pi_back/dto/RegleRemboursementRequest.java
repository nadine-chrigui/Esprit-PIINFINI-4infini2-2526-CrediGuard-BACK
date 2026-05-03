package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.RegleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegleRemboursementRequest {

    @NotNull(message = "typeRegle is required")
    private RegleType typeRegle;

    @NotNull(message = "valeur is required")
    @Positive(message = "valeur must be > 0")
    private Double valeur;

    @NotNull(message = "creditId is required")
    private Long creditId;
}

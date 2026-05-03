package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.RegleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegleRemboursementResponse {

    private Long idRegle;
    private RegleType typeRegle;
    private Double valeur;
    private Long creditId;
}

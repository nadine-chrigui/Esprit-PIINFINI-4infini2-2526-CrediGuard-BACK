package tn.esprit.pi_back.dto.insurance;

import lombok.Getter;
import lombok.Setter;
import tn.esprit.pi_back.entities.enums.TypeCredit;

@Getter
@Setter
public class InsuranceSimulationDTO {
    private Double amount;
    private Integer durationInMonths;
    private TypeCredit loanType;
    private Integer clientAge;
}

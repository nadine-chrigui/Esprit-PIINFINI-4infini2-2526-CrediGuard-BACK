package tn.esprit.pi_back.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsurancePolicyDTO {
    private Long id;
    private String policyNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private InsuranceCompanyMiniDTO insuranceCompany;
    private ClientDTO client;
    private String status;
    private Double premiumAmount;
    private InsuranceOfferDTO insuranceOffer;
}
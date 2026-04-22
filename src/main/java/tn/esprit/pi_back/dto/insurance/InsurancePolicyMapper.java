package tn.esprit.pi_back.dto.insurance;

import tn.esprit.pi_back.entities.InsurancePolicy;

public class InsurancePolicyMapper {

    public static InsurancePolicyDTO toDTO(InsurancePolicy p) {
        return new InsurancePolicyDTO(
                p.getId(),
                p.getPolicyNumber(),
                p.getStartDate(),
                p.getEndDate(),
                p.getInsuranceCompany() != null ? new InsuranceCompanyMiniDTO(
                        p.getInsuranceCompany().getId(),
                        p.getInsuranceCompany().getName()
                ) : null,
                UserMapper.toClientDTO(p.getClient())
        );
    }
}

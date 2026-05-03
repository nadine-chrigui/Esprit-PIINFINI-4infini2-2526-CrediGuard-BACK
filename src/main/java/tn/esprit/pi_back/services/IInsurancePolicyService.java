package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.InsurancePolicy;

import java.util.List;

public interface IInsurancePolicyService {

    InsurancePolicy addAndAssign(Long idCompany, Long idClient, InsurancePolicy policy);

    InsurancePolicy update(InsurancePolicy policy);

    void delete(Long id);

    InsurancePolicy get(Long id);

    List<InsurancePolicy> all();

    InsurancePolicy getPolicyByUserId(Long userId); // ancienne (on garde)

    List<InsurancePolicy> getPoliciesByUserId(Long userId); // 🔥 nouvelle

}
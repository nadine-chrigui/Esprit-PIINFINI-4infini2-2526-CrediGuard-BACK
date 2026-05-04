package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.InsurancePolicy;
import java.util.List;

public interface IInsurancePolicyService {
    InsurancePolicy createContract(Long clientId, Long offerId, Double declaredValue, String goodsDescription, String voucherCode, java.time.LocalDate startDate);
    InsurancePolicy renewContract(Long policyId);
    List<InsurancePolicy> getClientPolicies(Long clientId);
    InsurancePolicy getById(Long id);
    void checkExpiringPolicies();

    // Legacy support methods for InsurancePolicyRestController
    InsurancePolicy addAndAssign(Long companyId, Long clientId, InsurancePolicy policy);
    InsurancePolicy update(InsurancePolicy policy);
    void delete(Long id);
    InsurancePolicy get(Long id);
    List<InsurancePolicy> all();
    InsurancePolicy getPolicyByUserId(Long userId);
    List<InsurancePolicy> getPoliciesByUserId(Long userId);
}
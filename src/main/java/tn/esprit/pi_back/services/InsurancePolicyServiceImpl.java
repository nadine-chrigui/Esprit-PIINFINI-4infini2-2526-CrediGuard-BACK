package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.InsuranceCompany;
import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.InsuranceCompanyRepository;
import tn.esprit.pi_back.repositories.InsurancePolicyRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsurancePolicyServiceImpl implements IInsurancePolicyService {

    private final InsurancePolicyRepository policyRepo;
    private final InsuranceCompanyRepository companyRepo;
    private final UserRepository userRepo;

    @Override
    public InsurancePolicy addAndAssign(Long idCompany, Long idClient, InsurancePolicy policy) {
        InsuranceCompany company = companyRepo.findById(idCompany).orElse(null);
        User client = userRepo.findById(idClient).orElse(null);
        if (company == null || client == null) return null;

        policy.setInsuranceCompany(company);
        policy.setClient(client);

        return policyRepo.save(policy);
    }

    @Override
    public InsurancePolicy update(InsurancePolicy policy) {

        InsurancePolicy existing = policyRepo.findById(policy.getId()).orElse(null);
        if (existing == null) return null;

        existing.setPolicyNumber(policy.getPolicyNumber());
        existing.setStartDate(policy.getStartDate());
        existing.setEndDate(policy.getEndDate());

        return policyRepo.save(existing);
    }

    @Override
    public void delete(Long id) {
        policyRepo.deleteById(id);
    }

    @Override
    public InsurancePolicy get(Long id) {
        return policyRepo.findById(id).orElse(null);
    }

    @Override
    public List<InsurancePolicy> all() {
        return policyRepo.findAll();
    }

    // 🔥 garde compatibilité (ancienne méthode)
    @Override
    public InsurancePolicy getPolicyByUserId(Long userId) {
        List<InsurancePolicy> policies = policyRepo.findByClientId(userId);
        return policies.isEmpty() ? null : policies.get(0);
    }

    // 🔥 NOUVELLE MÉTHODE (IMPORTANT)
    @Override
    public List<InsurancePolicy> getPoliciesByUserId(Long userId) {
        return policyRepo.findByClientId(userId);
    }
}
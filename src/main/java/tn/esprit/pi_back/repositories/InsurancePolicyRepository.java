package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.InsurancePolicy;

import java.util.List;

public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Long> {

    List<InsurancePolicy> findByClientId(Long clientId);
    List<InsurancePolicy> findByEndDateBetween(java.time.LocalDate start, java.time.LocalDate end);
    boolean existsByPolicyNumber(String policyNumber);

}
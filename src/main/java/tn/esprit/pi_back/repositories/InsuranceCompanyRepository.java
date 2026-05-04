package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.InsuranceCompany;
import java.util.List;

public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompany, Long> {
    List<InsuranceCompany> findByActiveTrue();
}
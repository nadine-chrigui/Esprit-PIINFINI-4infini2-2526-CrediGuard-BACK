package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.InsuranceOffer;

import java.util.List;

@Repository
public interface InsuranceOfferRepository extends JpaRepository<InsuranceOffer, Long> {
    List<InsuranceOffer> findByInsuranceCompanyId(Long companyId);
}

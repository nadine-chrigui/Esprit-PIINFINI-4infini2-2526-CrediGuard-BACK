package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.InsuranceRecommendation;

import java.util.Optional;

@Repository
public interface InsuranceRecommendationRepository extends JpaRepository<InsuranceRecommendation, Long> {
    Optional<InsuranceRecommendation> findByDemandeCreditId(Long demandeCreditId);
    java.util.List<InsuranceRecommendation> findByDemandeCreditClientId(Long clientId);
}

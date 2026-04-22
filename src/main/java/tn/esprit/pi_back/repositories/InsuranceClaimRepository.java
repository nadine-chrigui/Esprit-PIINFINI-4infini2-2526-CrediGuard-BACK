package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.InsuranceClaim;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {
}
package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.PlanUtilisationCredit;

import java.util.Optional;

public interface PlanUtilisationRepository extends JpaRepository<PlanUtilisationCredit, Long> {

    Optional<PlanUtilisationCredit> findByDemandeCreditId(Long demandeId);

    boolean existsByDemandeCreditId(Long demandeId);
}
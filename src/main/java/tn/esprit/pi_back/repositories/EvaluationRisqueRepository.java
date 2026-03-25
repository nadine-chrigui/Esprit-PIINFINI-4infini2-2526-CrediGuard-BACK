package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.EvaluationRisque;

import java.util.Optional;

public interface EvaluationRisqueRepository extends JpaRepository<EvaluationRisque, Long> {

    Optional<EvaluationRisque> findByDemandeCreditId(Long demandeId);

    boolean existsByDemandeCreditId(Long demandeId);
}
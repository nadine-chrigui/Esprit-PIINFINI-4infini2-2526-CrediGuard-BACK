package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.DecisionCredit;

import java.util.Optional;

public interface DecisionCreditRepository extends JpaRepository<DecisionCredit, Long> {

    Optional<DecisionCredit> findByDemandeCreditId(Long demandeId);

    boolean existsByDemandeCreditId(Long demandeId);
}
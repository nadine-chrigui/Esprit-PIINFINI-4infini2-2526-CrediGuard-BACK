package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.enums.StatutCredit;

import java.util.List;
import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long> {

    List<Credit> findByClientId(Long clientId);

    List<Credit> findByStatut(StatutCredit statut);
    boolean existsByDemandeCreditId(Long demandeId);
    List<Credit> findByClientIdAndStatut(Long clientId, StatutCredit statut);
    Optional<Credit> findByDemandeCreditId(Long demandeId);
}
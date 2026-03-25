package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.enums.StatutDemande;

import java.util.List;
import java.util.Optional;

public interface DemandeCreditRepository extends JpaRepository<DemandeCredit, Long> {

    Optional<DemandeCredit> findByReference(String reference);
    boolean existsByReference(String reference);

    List<DemandeCredit> findByClientId(Long clientId);
    List<DemandeCredit> findByStatut(StatutDemande statut);
    List<DemandeCredit> findByClientIdAndStatut(Long clientId, StatutDemande statut);
}
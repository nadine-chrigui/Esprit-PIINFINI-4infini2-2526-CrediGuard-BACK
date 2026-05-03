package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import java.util.List;
import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    List<Credit> findByClientId(Long clientId);
    List<Credit> findByStatut(StatutCredit statut);
    boolean existsByDemandeCreditId(Long demandeId);
    List<Credit> findByClientIdAndStatut(Long clientId, StatutCredit statut);
    
    @Query("SELECT c FROM Credit c WHERE c.compte.idCompte = :idCompte AND c.statut = :statut")
    List<Credit> findByCompteIdCompteAndStatut(@Param("idCompte") Long idCompte, @Param("statut") StatutCredit statut);
    
    Optional<Credit> findByDemandeCreditId(Long demandeId);

    @Query("SELECT SUM(c.montantAccorde) FROM Credit c") 
    Double sumTotalAmountGranted();
    
    @Query("SELECT SUM(c.montantRestant) FROM Credit c") 
    Double sumTotalAmountRemaining();
    
    long countByStatut(StatutCredit statut);
}
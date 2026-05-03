package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.compteSource.idCompte = :sourceId OR t.compteDestination.idCompte = :destId")
    List<Transaction> findByCompteSourceIdCompteOrCompteDestinationIdCompte(@Param("sourceId") Long sourceId, @Param("destId") Long destId);

    List<Transaction> findByCompteSource_IdCompte(Long sourceId);
    
    long countByStatut(TransactionStatut statut);
}

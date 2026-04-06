package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.entities.enums.TransactionStatut;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCompteSourceIdCompteOrCompteDestinationIdCompte(Long compteSourceId, Long compteDestinationId);

    @Query("select t.idTransaction from Transaction t where t.compteSource.idCompte = :compteId or t.compteDestination.idCompte = :compteId")
    List<Long> findIdsByCompteId(@Param("compteId") Long compteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Transaction t where t.compteSource.idCompte = :compteId or t.compteDestination.idCompte = :compteId")
    int deleteByCompteId(@Param("compteId") Long compteId);

    long countByStatut(TransactionStatut statut);
}

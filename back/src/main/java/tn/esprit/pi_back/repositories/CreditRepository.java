package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.enums.StatutCredit;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    long countByStatut(StatutCredit statut);

    @Query("SELECT SUM(c.montantAccorde) FROM Credit c")
    Double sumTotalAmountGranted();

    @Query("SELECT SUM(c.montantRestant) FROM Credit c")
    Double sumTotalAmountRemaining();
}

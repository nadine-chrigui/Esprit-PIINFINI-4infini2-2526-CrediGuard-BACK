package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.Remboursement;
import java.util.List;

public interface RemboursementRepository extends JpaRepository<Remboursement, Long> {

    @Query("SELECT r FROM Remboursement r WHERE r.credit.id = :creditId")
    List<Remboursement> findByCreditId(@Param("creditId") Long creditId);
}
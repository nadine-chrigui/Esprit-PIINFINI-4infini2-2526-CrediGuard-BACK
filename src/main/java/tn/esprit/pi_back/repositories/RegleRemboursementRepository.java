package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.RegleRemboursement;
import java.util.List;

public interface RegleRemboursementRepository extends JpaRepository<RegleRemboursement, Long> {

    @Query("SELECT r FROM RegleRemboursement r WHERE r.credit.id = :creditId")
    List<RegleRemboursement> findByCreditId(@Param("creditId") Long creditId);
}
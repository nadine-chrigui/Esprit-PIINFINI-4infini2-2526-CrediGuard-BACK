package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.RegleRemboursement;

import java.util.List;

public interface RegleRemboursementRepository extends JpaRepository<RegleRemboursement, Long> {

    List<RegleRemboursement> findByCreditId(Long creditId);
}

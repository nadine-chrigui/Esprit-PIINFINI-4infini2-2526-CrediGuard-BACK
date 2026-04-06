package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.DemandeCredit;

public interface DemandeCreditRepository extends JpaRepository<DemandeCredit, Long> {
}

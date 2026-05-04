package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.Modalite;

import java.util.Optional;

public interface ModaliteRepository extends JpaRepository<Modalite, Long> {
    Optional<Modalite> findByDemandeCreditId(Long demandeId);
    boolean existsByDemandeCreditId(Long demandeId);
}

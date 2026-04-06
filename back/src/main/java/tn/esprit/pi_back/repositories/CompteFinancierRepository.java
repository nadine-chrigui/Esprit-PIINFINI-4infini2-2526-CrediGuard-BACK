package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.CompteFinancier;

import java.util.Optional;

public interface CompteFinancierRepository extends JpaRepository<CompteFinancier, Long> {

    Optional<CompteFinancier> findByUtilisateurId(Long utilisateurId);
}

package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.CompteFinancier;
import java.util.Optional;

@Repository
public interface CompteFinancierRepository extends JpaRepository<CompteFinancier, Long> {
    
    Optional<CompteFinancier> findByUtilisateur_Id(Long userId);

    @Query("SELECT c FROM CompteFinancier c WHERE c.utilisateur.id = :userId")
    Optional<CompteFinancier> findByUtilisateurId(@Param("userId") Long userId);
}

package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.Vente;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {
}

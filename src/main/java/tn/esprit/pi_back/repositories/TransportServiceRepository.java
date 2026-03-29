package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.TransportService;

@Repository
public interface TransportServiceRepository extends JpaRepository<TransportService, Long> {
}

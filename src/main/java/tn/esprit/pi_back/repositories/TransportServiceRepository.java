package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.TransportService;

import java.util.List;

public interface TransportServiceRepository extends JpaRepository<TransportService, Long> {

    List<TransportService> findByEventId(Long eventId);
}

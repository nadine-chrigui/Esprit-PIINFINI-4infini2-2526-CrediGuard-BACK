package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tn.esprit.pi_back.entities.TransportService;

import java.util.List;

@RepositoryRestResource(path = "transport-services")
public interface TransportServiceRepository extends JpaRepository<TransportService, Long> {

    List<TransportService> findByEventId(Long eventId);

    List<TransportService> findByStatus(String status);
}
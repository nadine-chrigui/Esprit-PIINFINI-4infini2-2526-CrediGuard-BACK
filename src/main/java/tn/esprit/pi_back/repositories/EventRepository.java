package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tn.esprit.pi_back.entities.Event;

import java.util.List;

@RepositoryRestResource(path = "events")
public interface EventRepository extends JpaRepository<Event, Long> {

    // Exemples de méthodes de recherche supplémentaires
    List<Event> findByStatus(String status);

    List<Event> findByEventType(String eventType);
}
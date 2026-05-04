package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}

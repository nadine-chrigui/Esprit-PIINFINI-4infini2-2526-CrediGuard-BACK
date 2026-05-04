package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.EventRegistration;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
    long countByEventId(Long eventId);
    List<EventRegistration> findByEventId(Long eventId);

    @Query("SELECT e FROM Event e JOIN EventRegistration er ON e.id = er.event.id WHERE er.user.id = :userId")
    List<Event> findEventsByUserId(Long userId);
}

package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.CalendarEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("""
        select e from CalendarEvent e
        where e.active = true
          and e.startDate <= :now
          and e.endDate >= :now
        order by e.startDate asc
    """)
    List<CalendarEvent> findActiveEventsAt(@Param("now") LocalDateTime now);
}
package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.calendar_event.CalendarEventResponse;
import tn.esprit.pi_back.entities.CalendarEvent;

@Component
public class CalendarEventMapper {

    public CalendarEventResponse toResponse(CalendarEvent event) {
        return new CalendarEventResponse(
                event.getId(),
                event.getName(),
                event.getCode(),
                event.getDescription(),
                event.getEventType(),
                event.getStartDate(),
                event.getEndDate(),
                event.getRecurringAnnually(),
                event.getActive(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
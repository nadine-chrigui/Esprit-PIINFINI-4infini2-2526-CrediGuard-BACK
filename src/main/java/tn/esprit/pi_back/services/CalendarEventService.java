package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.calendar_event.CalendarEventCreateRequest;
import tn.esprit.pi_back.dto.calendar_event.CalendarEventResponse;

import java.util.List;

public interface CalendarEventService {
    CalendarEventResponse create(CalendarEventCreateRequest request);
    List<CalendarEventResponse> getAll();
    List<CalendarEventResponse> getActive();
    CalendarEventResponse getById(Long id);
}
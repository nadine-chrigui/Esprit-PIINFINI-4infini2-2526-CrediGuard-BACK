package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.calendar_event.CalendarEventCreateRequest;
import tn.esprit.pi_back.dto.calendar_event.CalendarEventResponse;
import tn.esprit.pi_back.services.CalendarEventService;

import java.util.List;

@RestController
@RequestMapping("/calendar-events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @PostMapping
    public CalendarEventResponse create(@Valid @RequestBody CalendarEventCreateRequest request) {
        return calendarEventService.create(request);
    }

    @GetMapping
    public List<CalendarEventResponse> getAll() {
        return calendarEventService.getAll();
    }

    @GetMapping("/active")
    public List<CalendarEventResponse> getActive() {
        return calendarEventService.getActive();
    }

    @GetMapping("/{id}")
    public CalendarEventResponse getById(@PathVariable Long id) {
        return calendarEventService.getById(id);
    }
}
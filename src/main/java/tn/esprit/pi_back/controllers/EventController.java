package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.EventParticipation;
import tn.esprit.pi_back.services.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // ---- Event CRUD ----

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ---- EventParticipation ----

    @GetMapping("/{eventId}/participations")
    public List<EventParticipation> getParticipationsByEvent(@PathVariable Long eventId) {
        return eventService.getParticipationsByEvent(eventId);
    }

    @GetMapping("/beneficiaries/{beneficiaryId}/participations")
    public List<EventParticipation> getParticipationsByBeneficiary(@PathVariable Long beneficiaryId) {
        return eventService.getParticipationsByBeneficiary(beneficiaryId);
    }

    @PostMapping("/participations")
    public EventParticipation createParticipation(@RequestBody EventParticipation participation) {
        return eventService.createParticipation(participation);
    }

    @DeleteMapping("/participations/{id}")
    public ResponseEntity<Void> deleteParticipation(@PathVariable Long id) {
        eventService.deleteParticipation(id);
        return ResponseEntity.noContent().build();
    }
}

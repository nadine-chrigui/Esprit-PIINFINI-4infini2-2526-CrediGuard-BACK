package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.EventParticipationRequestDto;
import tn.esprit.pi_back.dto.EventParticipationResponseDto;
import tn.esprit.pi_back.dto.EventRequestDto;
import tn.esprit.pi_back.dto.EventResponseDto;
import tn.esprit.pi_back.services.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ========================= Event =========================

    // CREATE
    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto event) {
        return ResponseEntity.ok(eventService.createEvent(event));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequestDto event) {
        return ResponseEntity.ok(eventService.updateEvent(id, event));
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== EventParticipation ====================

    // CREATE
    @PostMapping("/participations")
    public ResponseEntity<EventParticipationResponseDto> createParticipation(@Valid @RequestBody EventParticipationRequestDto participation) {
        return ResponseEntity.ok(eventService.createParticipation(participation));
    }

    // UPDATE
    @PutMapping("/participations/{id}")
    public ResponseEntity<EventParticipationResponseDto> updateParticipation(@PathVariable Long id,
                                                                             @Valid @RequestBody EventParticipationRequestDto participation) {
        return ResponseEntity.ok(eventService.updateParticipation(id, participation));
    }

    // GET BY ID
    @GetMapping("/participations/{id}")
    public ResponseEntity<EventParticipationResponseDto> getParticipationById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getParticipationById(id));
    }

    // GET ALL
    @GetMapping("/participations")
    public ResponseEntity<List<EventParticipationResponseDto>> getAllParticipations() {
        return ResponseEntity.ok(eventService.getAllParticipations());
    }

    // DELETE
    @DeleteMapping("/participations/{id}")
    public ResponseEntity<Void> deleteParticipation(@PathVariable Long id) {
        eventService.deleteParticipation(id);
        return ResponseEntity.noContent().build();
    }
}

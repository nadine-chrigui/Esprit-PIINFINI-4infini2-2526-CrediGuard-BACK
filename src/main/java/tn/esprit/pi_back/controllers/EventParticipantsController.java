package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.EventRegistration;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.services.EventParticipantsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EventParticipantsController {

    private final EventParticipantsService participantsService;

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<?> getEventParticipants(@PathVariable Long eventId) {
        System.out.println("=== DEBUG getEventParticipants CONTROLLER ===");
        System.out.println("eventId: " + eventId);
        
        try {
            List<User> participants = participantsService.getEventParticipants(eventId);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{eventId}/participants/count")
    public ResponseEntity<?> getParticipantsCount(@PathVariable Long eventId) {
        try {
            long count = participantsService.getParticipantsCount(eventId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{eventId}/participants/details")
    public ResponseEntity<?> getRegistrationDetails(@PathVariable Long eventId) {
        try {
            List<EventRegistration> details = participantsService.getRegistrationDetails(eventId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.services.EventRegistrationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerToEvent(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {
        try {
            registrationService.registerUser(user.getEmail(), id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace(); // Affiche l'erreur dans la console du backend
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<Void> unregisterFromEvent(@PathVariable Long id,
                                                    @AuthenticationPrincipal User user) {
        registrationService.unregisterUser(user.getEmail(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-registrations")
    public ResponseEntity<?> getMyRegistrations(@AuthenticationPrincipal User user) {
        System.out.println("=== DEBUG getMyRegistrations ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        try {
            List<Event> events = registrationService.getEventsRegisteredByUser(user.getEmail());
            System.out.println("Events retournés: " + events.size());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
            e.printStackTrace(); // Affiche l'erreur dans la console du backend
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/is-registered")
    public ResponseEntity<Map<String, Boolean>> isRegistered(@PathVariable Long id,
                                                           @AuthenticationPrincipal User user) {
        boolean registered = registrationService.isUserRegistered(user.getEmail(), id);
        return ResponseEntity.ok(Map.of("registered", registered));
    }
}

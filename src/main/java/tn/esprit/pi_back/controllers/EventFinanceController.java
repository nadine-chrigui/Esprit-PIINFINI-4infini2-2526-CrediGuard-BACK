package tn.esprit.pi_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.services.EventFinanceService;
import tn.esprit.pi_back.dtos.EventProfitability;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events/finance")
@CrossOrigin(origins = "http://localhost:4200")
public class EventFinanceController {

    public EventFinanceController() {
        System.out.println("=== EventFinanceController CHARGÉ ===");
    }

    @Autowired
    private EventFinanceService financeService;

    @GetMapping("/{eventId}/profitability")
    public ResponseEntity<?> getEventProfitability(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user) {
        
        System.out.println("=== APPEL getEventProfitability ===");
        System.out.println("eventId: " + eventId);
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        
        try {
            EventProfitability profitability = financeService.calculateProfitability(eventId);
            System.out.println("Calcul réussi: " + profitability);
            return ResponseEntity.ok(profitability);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur IllegalArgumentException: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Erreur Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors du calcul: " + e.getMessage()));
        }
    }

    @GetMapping("/my-events/profitability")
    public ResponseEntity<?> getMyEventsProfitability(@AuthenticationPrincipal User user) {
        try {
            // TODO: Implémenter la récupération des événements créés par l'utilisateur
            // Pour l'instant, on retourne une liste vide
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getFinanceDashboard(@AuthenticationPrincipal User user) {
        try {
            // TODO: Implémenter le dashboard financier
            return ResponseEntity.ok(Map.of("message", "Dashboard en cours de développement"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug/events")
    public ResponseEntity<?> debugEvents() {
        try {
            List<Event> events = financeService.getAllEvents();
            return ResponseEntity.ok(Map.of("events", events, "count", events.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

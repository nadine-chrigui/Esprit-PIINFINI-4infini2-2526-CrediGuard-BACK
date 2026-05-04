package tn.esprit.pi_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dtos.EventFeedbackSummary;
import tn.esprit.pi_back.dtos.FeedbackDTO;
import tn.esprit.pi_back.entities.EventFeedback;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.services.EventFeedbackService;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/feedback")
@CrossOrigin(origins = "http://localhost:4200")
public class EventFeedbackController {

    @Autowired
    private EventFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long eventId,
            @RequestBody FeedbackDTO feedbackDTO) {
        
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Utilisateur non authentifié");
            }

            // Pour l'instant, on utilise un ID fixe. À adapter selon votre système d'auth
            Long userId = getCurrentUserId(authentication);
            
            EventFeedback feedback = feedbackService.submitFeedback(eventId, userId, feedbackDTO);
            return ResponseEntity.ok(feedback);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la soumission du feedback");
        }
    }

    @GetMapping
    public ResponseEntity<EventFeedbackSummary> getFeedbackSummary(@PathVariable Long eventId) {
        EventFeedbackSummary summary = feedbackService.getFeedbackSummary(eventId);
        return summary != null ? ResponseEntity.ok(summary) : ResponseEntity.noContent().build();
    }

    @GetMapping("/my-feedback")
    public ResponseEntity<List<EventFeedback>> getMyFeedback(@PathVariable Long eventId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = getCurrentUserId(authentication);
            
            List<EventFeedback> feedbacks = feedbackService.getUserFeedbackForEvent(eventId, userId);
            return ResponseEntity.ok(feedbacks);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/can-give-feedback")
    public ResponseEntity<Boolean> canGiveFeedback(@PathVariable Long eventId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = getCurrentUserId(authentication);
            
            boolean canGive = feedbackService.canUserGiveFeedback(eventId, userId);
            return ResponseEntity.ok(canGive);
            
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/all-summaries")
    public ResponseEntity<List<EventFeedbackSummary>> getAllFeedbackSummaries() {
        try {
            List<EventFeedbackSummary> summaries = feedbackService.getAllEventsFeedbackSummaries();
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Méthode temporaire pour récupérer l'ID utilisateur
    // À adapter selon votre implémentation d'authentification
    private Long getCurrentUserId(Authentication authentication) {
        // Pour le développement, retourne un ID fixe qui existe dans la base
        // En production, récupérez l'ID depuis l'utilisateur authentifié
        try {
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                Long userId = ((User) authentication.getPrincipal()).getId();
                System.out.println("DEBUG: User ID from auth: " + userId);
                return userId;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Auth error, using default ID: " + e.getMessage());
        }
        
        // ID par défaut pour le développement - assurez-vous qu'il existe dans votre base
        Long defaultUserId = 9L; // Changez cet ID si nécessaire
        System.out.println("DEBUG: Using default user ID: " + defaultUserId);
        return defaultUserId;
    }
}

package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarAuthUrlResponse;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarCodeExchangeRequest;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarConnectionStatusResponse;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarSyncResponse;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.services.GoogleCalendarService;

@RestController
@RequestMapping("/google-calendar")
@RequiredArgsConstructor
@CrossOrigin("*")
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;
    private final UserRepository userRepository;

    @GetMapping("/oauth/url")
    public ResponseEntity<GoogleCalendarAuthUrlResponse> getAuthorizationUrl(@RequestParam String redirectUri) {
        return ResponseEntity.ok(googleCalendarService.buildAuthorizationUrl(redirectUri));
    }

    @PostMapping("/oauth/exchange")
    public ResponseEntity<GoogleCalendarConnectionStatusResponse> exchangeCode(@Valid @RequestBody GoogleCalendarCodeExchangeRequest request,
                                                                               Authentication authentication) {
        return ResponseEntity.ok(
                googleCalendarService.exchangeAuthorizationCode(currentUserId(authentication), request.code(), request.redirectUri())
        );
    }

    @GetMapping("/status")
    public ResponseEntity<GoogleCalendarConnectionStatusResponse> getStatus(Authentication authentication) {
        return ResponseEntity.ok(googleCalendarService.getConnectionStatus(currentUserId(authentication)));
    }

    @PostMapping("/projects/{projectId}/sync")
    public ResponseEntity<GoogleCalendarSyncResponse> syncProject(@PathVariable Long projectId,
                                                                  Authentication authentication) {
        return ResponseEntity.ok(googleCalendarService.syncProjectCalendar(currentUserId(authentication), projectId));
    }

    @PostMapping("/investments/{investmentId}/sync")
    public ResponseEntity<GoogleCalendarSyncResponse> syncInvestment(@PathVariable Long investmentId,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(googleCalendarService.syncInvestmentCalendar(currentUserId(authentication), investmentId));
    }

    private Long currentUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getId();
    }
}

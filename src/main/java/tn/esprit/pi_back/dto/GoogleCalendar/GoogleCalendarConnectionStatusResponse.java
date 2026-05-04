package tn.esprit.pi_back.dto.GoogleCalendar;

import java.time.LocalDateTime;

public record GoogleCalendarConnectionStatusResponse(
        boolean connected,
        String googleEmail,
        LocalDateTime accessTokenExpiresAt
) {}

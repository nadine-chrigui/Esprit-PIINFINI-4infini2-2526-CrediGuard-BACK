package tn.esprit.pi_back.dto.GoogleCalendar;

import jakarta.validation.constraints.NotBlank;

public record GoogleCalendarCodeExchangeRequest(
        @NotBlank(message = "code is required")
        String code,
        @NotBlank(message = "redirectUri is required")
        String redirectUri
) {}

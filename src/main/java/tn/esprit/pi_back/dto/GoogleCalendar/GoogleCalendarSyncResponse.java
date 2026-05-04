package tn.esprit.pi_back.dto.GoogleCalendar;

public record GoogleCalendarSyncResponse(
        String googleEmail,
        int syncedEvents
) {}

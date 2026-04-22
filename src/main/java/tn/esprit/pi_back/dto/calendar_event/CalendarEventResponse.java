package tn.esprit.pi_back.dto.calendar_event;

import tn.esprit.pi_back.entities.enums.CalendarEventType;

import java.time.LocalDateTime;

public record CalendarEventResponse(
        Long id,
        String name,
        String code,
        String description,
        CalendarEventType eventType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean recurringAnnually,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
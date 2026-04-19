package tn.esprit.pi_back.dto.calendar_event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.esprit.pi_back.entities.enums.CalendarEventType;

import java.time.LocalDateTime;

public record CalendarEventCreateRequest(

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must not exceed 120 characters")
        String name,

        @NotBlank(message = "code is required")
        @Size(max = 80, message = "code must not exceed 80 characters")
        String code,

        @Size(max = 500, message = "description too long")
        String description,

        @NotNull(message = "eventType is required")
        CalendarEventType eventType,

        @NotNull(message = "startDate is required")
        LocalDateTime startDate,

        @NotNull(message = "endDate is required")
        LocalDateTime endDate,

        Boolean recurringAnnually,
        Boolean active
) {}
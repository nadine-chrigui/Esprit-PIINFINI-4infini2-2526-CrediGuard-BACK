package tn.esprit.pi_back.dto.ReturnPayment;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReturnPaymentResponse(
        Long returnId,
        Double amount,
        LocalDate paymentDate,
        String type,
        String status,
        Long investmentId,
        String currency,
        String stripePaymentIntentId,
        LocalDateTime confirmedAt,
        String googleCalendarEventLink,
        LocalDateTime googleCalendarSyncedAt
) {}

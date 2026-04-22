package tn.esprit.pi_back.dto.order;

import tn.esprit.pi_back.entities.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderAdminResponse(
        Long id,
        Long userId,
        String clientName,
        String clientEmail,
        OrderStatus status,
        Double totalAmount,
        Long promoCodeId,
        String financeReference,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer itemCount
) {}
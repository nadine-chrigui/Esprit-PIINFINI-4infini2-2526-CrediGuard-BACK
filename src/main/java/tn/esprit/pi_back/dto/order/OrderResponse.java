package tn.esprit.pi_back.dto.order;

import tn.esprit.pi_back.entities.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;


public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        Double totalAmount,
        Long promoCodeId,
        String financeReference,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items
) {}
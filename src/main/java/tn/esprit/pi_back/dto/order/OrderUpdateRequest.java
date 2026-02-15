package tn.esprit.pi_back.dto.order;

import tn.esprit.pi_back.entities.enums.OrderStatus;


public record OrderUpdateRequest(
        OrderStatus status,
        Long promoCodeId,
        String financeReference
) {}
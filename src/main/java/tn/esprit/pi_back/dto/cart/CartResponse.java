package tn.esprit.pi_back.dto.cart;

import tn.esprit.pi_back.entities.enums.CartStatus;

import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        CartStatus status,
        List<CartItemResponse> items,
        Double subtotal,
        Double totalDiscount,
        Double total
) {}
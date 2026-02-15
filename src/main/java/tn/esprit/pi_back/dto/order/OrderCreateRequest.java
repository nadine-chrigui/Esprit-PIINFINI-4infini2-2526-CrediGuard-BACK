package tn.esprit.pi_back.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        Long promoCodeId,
        List<OrderItemCreateRequest> items
) {}
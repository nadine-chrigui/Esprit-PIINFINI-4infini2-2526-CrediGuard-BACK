package tn.esprit.pi_back.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        String promoCode,

        @NotEmpty(message = "items are required")
        List<@Valid OrderItemCreateRequest> items
) {}
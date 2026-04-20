package tn.esprit.pi_back.dto.order;

import jakarta.validation.constraints.NotNull;
import tn.esprit.pi_back.entities.enums.OrderStatus;

public record OrderStatusUpdateRequest(
        @NotNull OrderStatus status
) {}
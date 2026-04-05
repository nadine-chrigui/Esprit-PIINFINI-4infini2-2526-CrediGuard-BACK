package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.order.OrderAdminResponse;
import tn.esprit.pi_back.dto.order.OrderItemResponse;
import tn.esprit.pi_back.dto.order.OrderResponse;
import tn.esprit.pi_back.entities.Order;
import tn.esprit.pi_back.entities.OrderItem;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = (order.getItems() == null)
                ? List.of()
                : order.getItems().stream().map(this::toItemResponse).toList();

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getStatus(),
                order.getTotalAmount(),
                order.getPromoCode() != null ? order.getPromoCode().getId() : null,
                order.getFinanceReference(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    public OrderItemResponse toItemResponse(OrderItem it) {
        return new OrderItemResponse(
                it.getId(),
                it.getProduct() != null ? it.getProduct().getId() : null,
                it.getProduct() != null ? it.getProduct().getName() : null,
                it.getQuantity(),
                it.getUnitPrice(),
                it.getLineTotal()
        );
    }

    public OrderAdminResponse toAdminResponse(Order order) {
        String clientName = null;
        String clientEmail = null;

        if (order.getUser() != null) {
            clientName = order.getUser().getFullName();
            clientEmail = order.getUser().getEmail();
        }

        int itemCount = (order.getItems() != null) ? order.getItems().size() : 0;

        return new OrderAdminResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                clientName,
                clientEmail,
                order.getStatus(),
                order.getTotalAmount(),
                order.getPromoCode() != null ? order.getPromoCode().getId() : null,
                order.getFinanceReference(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemCount
        );
    }
}
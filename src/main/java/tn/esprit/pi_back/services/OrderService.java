package tn.esprit.pi_back.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.pi_back.dto.order.OrderAdminResponse;
import tn.esprit.pi_back.dto.order.OrderCreateRequest;
import tn.esprit.pi_back.dto.order.OrderResponse;
import tn.esprit.pi_back.dto.order.OrderStatusUpdateRequest;
import tn.esprit.pi_back.dto.order.OrderUpdateRequest;
import tn.esprit.pi_back.entities.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderResponse create(OrderCreateRequest req);

    OrderResponse getById(Long id);

    List<OrderResponse> getMine();

    OrderResponse update(Long id, OrderUpdateRequest req);

    void delete(Long id);

    Page<OrderAdminResponse> getAllAdmin(
            OrderStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable
    );

    OrderResponse getAdminById(Long id);

    OrderResponse updateStatusAdmin(Long id, OrderStatusUpdateRequest req);
}
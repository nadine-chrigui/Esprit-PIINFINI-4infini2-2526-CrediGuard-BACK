package tn.esprit.pi_back.services;


import tn.esprit.pi_back.dto.order.OrderCreateRequest;
import tn.esprit.pi_back.dto.order.OrderResponse;
import tn.esprit.pi_back.dto.order.OrderUpdateRequest;

import java.util.List;
public interface OrderService
{
    OrderResponse create(OrderCreateRequest req);
    OrderResponse getById(Long id);
    List<OrderResponse> getMine();
    OrderResponse update(Long id, OrderUpdateRequest req);
    void delete(Long id);
}
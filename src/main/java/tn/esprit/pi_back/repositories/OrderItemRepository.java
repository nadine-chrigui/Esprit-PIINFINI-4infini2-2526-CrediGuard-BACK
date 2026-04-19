package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}
package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.Delivery;
import tn.esprit.pi_back.entities.enums.DeliveryStatus;

import java.util.Optional;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    // si tu veux récupérer toutes les deliveries d’un buyer
    List<Delivery> findByOrderUserId(Long userId);
    List<Delivery> findByDeliveryStatusOrderByCreatedAtDesc(DeliveryStatus deliveryStatus);

    long countByDeliveryStatus(DeliveryStatus deliveryStatus);
}

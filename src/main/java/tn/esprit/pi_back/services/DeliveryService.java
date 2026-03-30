package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.delivery.*;

import java.util.List;

public interface DeliveryService {
    DeliveryResponse create(DeliveryCreateRequest req);
    DeliveryResponse update(Long id, DeliveryUpdateRequest req);
    DeliveryResponse getById(Long id);
    DeliveryResponse getByOrderId(Long orderId);
    List<DeliveryResponse> getMine();
    void delete(Long id);
     List<DeliveryResponse> getAllDeliveries();
}
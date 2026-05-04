package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.deliveryzone.*;
import tn.esprit.pi_back.entities.DeliveryZone;

import java.util.List;
import java.util.Optional;

public interface DeliveryZoneService {
    DeliveryZoneResponse create(DeliveryZoneCreateRequest req);
    DeliveryZoneResponse update(Long id, DeliveryZoneUpdateRequest req);
    DeliveryZoneResponse getById(Long id);
    List<DeliveryZoneResponse> getAll();
    List<DeliveryZoneResponse> getActive();
    void delete(Long id);
    DeliveryZoneCheckResponse checkPoint(Double latitude, Double longitude);
    DeliveryFeeCheckResponse checkAddress(DeliveryFeeCheckRequest req);
    Optional<DeliveryZone> findMatchingZone(Double latitude, Double longitude);
}

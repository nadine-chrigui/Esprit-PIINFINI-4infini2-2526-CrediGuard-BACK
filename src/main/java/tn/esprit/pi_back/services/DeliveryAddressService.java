package tn.esprit.pi_back.services;



import tn.esprit.pi_back.dto.delivery.*;

import java.util.List;

public interface DeliveryAddressService {
    DeliveryAddressResponse create(DeliveryAddressCreateRequest req);
    DeliveryAddressResponse update(Long id, DeliveryAddressUpdateRequest req);
    DeliveryAddressResponse getById(Long id);
    List<DeliveryAddressResponse> getAll();
    void delete(Long id);
}
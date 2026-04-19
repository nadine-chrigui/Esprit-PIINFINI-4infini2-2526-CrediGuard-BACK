package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.delivery.*;
import tn.esprit.pi_back.entities.DeliveryAddress;
import tn.esprit.pi_back.repositories.DeliveryAddressRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryAddressServiceImpl implements DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;

    @Override
    public DeliveryAddressResponse create(DeliveryAddressCreateRequest req) {
        DeliveryAddress a = new DeliveryAddress();
        a.setFullName(req.fullName().trim());
        a.setPhone(req.phone().trim());
        a.setCity(req.city().trim());
        a.setAddressLine(req.addressLine().trim());
        a.setAdditionalInfo(req.additionalInfo());
        return toResponse(deliveryAddressRepository.save(a));
    }

    @Override
    public DeliveryAddressResponse update(Long id, DeliveryAddressUpdateRequest req) {
        DeliveryAddress a = deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DeliveryAddress not found: " + id));

        if (req.fullName() != null) a.setFullName(req.fullName().trim());
        if (req.phone() != null) a.setPhone(req.phone().trim());
        if (req.city() != null) a.setCity(req.city().trim());
        if (req.addressLine() != null) a.setAddressLine(req.addressLine().trim());
        if (req.additionalInfo() != null) a.setAdditionalInfo(req.additionalInfo());

        return toResponse(a);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryAddressResponse getById(Long id) {
        return deliveryAddressRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("DeliveryAddress not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryAddressResponse> getAll() {
        return deliveryAddressRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public void delete(Long id) {
        if (!deliveryAddressRepository.existsById(id)) {
            throw new IllegalArgumentException("DeliveryAddress not found: " + id);
        }
        deliveryAddressRepository.deleteById(id);
    }

    private DeliveryAddressResponse toResponse(DeliveryAddress a) {
        return new DeliveryAddressResponse(
                a.getId(),
                a.getFullName(),
                a.getPhone(),
                a.getCity(),
                a.getAddressLine(),
                a.getAdditionalInfo()
        );
    }
}
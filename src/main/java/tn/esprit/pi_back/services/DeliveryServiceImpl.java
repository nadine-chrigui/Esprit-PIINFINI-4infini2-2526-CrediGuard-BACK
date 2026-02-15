package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.delivery.*;
import tn.esprit.pi_back.entities.Delivery;
import tn.esprit.pi_back.entities.DeliveryAddress;
import tn.esprit.pi_back.entities.Order;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.DeliveryStatus;
import tn.esprit.pi_back.repositories.DeliveryAddressRepository;
import tn.esprit.pi_back.repositories.DeliveryRepository;
import tn.esprit.pi_back.repositories.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Override
    public DeliveryResponse create(DeliveryCreateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + req.orderId()));

        // sécurité: seul le buyer crée la delivery de sa commande
        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order.");
        }

        if (deliveryRepository.findByOrderId(order.getId()).isPresent()) {
            throw new IllegalArgumentException("Delivery already exists for order: " + order.getId());
        }

        DeliveryAddress address = deliveryAddressRepository.findById(req.addressId())
                .orElseThrow(() -> new IllegalArgumentException("DeliveryAddress not found: " + req.addressId()));

        Delivery d = new Delivery();
        d.setOrder(order);
        d.setAddress(address);

        d.setDeliveryType(req.deliveryType());
        d.setDeliveryStatus(DeliveryStatus.PENDING);

        d.setDeliverySlot(req.deliverySlot());
        d.setDeliveryFee(req.deliveryFee());

        d.setScheduledAt(req.scheduledAt());
        d.setTrackingNumber(req.trackingNumber());
        d.setCarrier(req.carrier());

        return toResponse(deliveryRepository.save(d));
    }

    @Override
    public DeliveryResponse update(Long id, DeliveryUpdateRequest req) {
        Delivery d = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + id));

        // sécurité: owner = buyer
        User me = userService.getOrCreateCurrentUser();
        if (!d.getOrder().getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your delivery.");
        }

        if (req.addressId() != null) {
            DeliveryAddress address = deliveryAddressRepository.findById(req.addressId())
                    .orElseThrow(() -> new IllegalArgumentException("DeliveryAddress not found: " + req.addressId()));
            d.setAddress(address);
        }

        if (req.deliveryType() != null) d.setDeliveryType(req.deliveryType());
        if (req.deliveryStatus() != null) d.setDeliveryStatus(req.deliveryStatus());
        if (req.deliverySlot() != null) d.setDeliverySlot(req.deliverySlot());
        if (req.deliveryFee() != null) d.setDeliveryFee(req.deliveryFee());

        if (req.scheduledAt() != null) d.setScheduledAt(req.scheduledAt());
        if (req.shippedAt() != null) d.setShippedAt(req.shippedAt());
        if (req.deliveredAt() != null) d.setDeliveredAt(req.deliveredAt());

        if (req.trackingNumber() != null) d.setTrackingNumber(req.trackingNumber());
        if (req.carrier() != null) d.setCarrier(req.carrier());

        return toResponse(d);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getById(Long id) {
        return deliveryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found for order: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getMine() {
        User me = userService.getOrCreateCurrentUser();
        return deliveryRepository.findByOrderUserId(me.getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public void delete(Long id) {
        Delivery d = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + id));

        User me = userService.getOrCreateCurrentUser();
        if (!d.getOrder().getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your delivery.");
        }

        // soft delete simple
        d.setDeliveryStatus(DeliveryStatus.CANCELLED);
    }

    private DeliveryResponse toResponse(Delivery d) {
        DeliveryAddress a = d.getAddress();
        DeliveryAddressResponse addr = a == null ? null : new DeliveryAddressResponse(
                a.getId(),
                a.getFullName(),
                a.getPhone(),
                a.getCity(),
                a.getAddressLine(),
                a.getAdditionalInfo()
        );

        return new DeliveryResponse(
                d.getId(),
                d.getOrder() != null ? d.getOrder().getId() : null,
                d.getDeliveryType(),
                d.getDeliveryStatus(),
                d.getDeliverySlot(),
                d.getDeliveryFee(),
                d.getScheduledAt(),
                d.getShippedAt(),
                d.getDeliveredAt(),
                d.getTrackingNumber(),
                d.getCarrier(),
                addr,
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
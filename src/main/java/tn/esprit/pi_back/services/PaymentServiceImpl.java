package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.Payment.PaymentCreateRequest;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.Payment.PaymentUpdateRequest;
import tn.esprit.pi_back.entities.Order;
import tn.esprit.pi_back.entities.Payment;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.PaymentStatus;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.repositories.PaymentRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Override
    public PaymentResponse create(PaymentCreateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + req.orderId()));

        // TODO traçabilité: vérifier order.getUser().getId() == me.getId() (quand tu envoies Order)
        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order");
        }
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new IllegalArgumentException("Payment already exists for this order");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Order totalAmount must be > 0 to create a payment");
        }

        Payment p = new Payment();
        p.setOrder(order);
        p.setPaymentType(req.paymentType());
        p.setPaymentStatus(PaymentStatus.PENDING);
        p.setAmount(order.getTotalAmount()); // important

        return toResponse(paymentRepository.save(p));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrder(Long orderId) {
        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));
        return toResponse(p);
    }

    @Override
    public PaymentResponse update(Long id, PaymentUpdateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        // TODO traçabilité via order.user
        if (p.getOrder() == null || p.getOrder().getUser() == null || !p.getOrder().getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your payment");
        }

        if (req.paymentStatus() != null) p.setPaymentStatus(req.paymentStatus());
        if (req.transactionRef() != null) p.setTransactionRef(req.transactionRef());
        if (p.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Payment already PAID");
        }
        return toResponse(paymentRepository.save(p));
    }

    @Override
    public void delete(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        // workflow : interdire delete si PAID (optionnel)
        if (p.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Cannot delete a PAID payment");
        }
        if (p.getOrder() == null || p.getOrder().getUser() == null || !p.getOrder().getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your payment");
        }

        // TODO traçabilité via order.user

        paymentRepository.delete(p);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder() != null ? p.getOrder().getId() : null,
                p.getAmount(),
                p.getPaymentType(),
                p.getPaymentStatus(),
                p.getTransactionRef(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
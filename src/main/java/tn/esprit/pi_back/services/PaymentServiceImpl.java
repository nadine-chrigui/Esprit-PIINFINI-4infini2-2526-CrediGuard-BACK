package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Payment.PaymentCreateRequest;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.Payment.PaymentUpdateRequest;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.entities.enums.PaymentStatus;
import tn.esprit.pi_back.entities.enums.SaleMode;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.mappers.PaymentMapper;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.repositories.PaymentRepository;
import tn.esprit.pi_back.repositories.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final PaymentMapper paymentMapper;
    private final ProductRepository productRepository;

    @Override
    public PaymentResponse create(PaymentCreateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + req.orderId()));

        if (!isAdmin(me) && !order.getUser().getId().equals(me.getId())) {
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
        p.setAmount(order.getTotalAmount());

        Payment saved = paymentRepository.save(p);
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        User me = userService.getOrCreateCurrentUser();

        if (isAdmin(me)) {
            return paymentRepository.findAll()
                    .stream()
                    .map(paymentMapper::toResponse)
                    .toList();
        }

        return paymentRepository.findAll()
                .stream()
                .filter(p -> p.getOrder() != null
                        && p.getOrder().getUser() != null
                        && p.getOrder().getUser().getId().equals(me.getId()))
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        if (!isAdmin(me) && (p.getOrder() == null || p.getOrder().getUser() == null || !p.getOrder().getUser().getId().equals(me.getId()))) {
            throw new SecurityException("Forbidden: not your payment");
        }

        return paymentMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrder(Long orderId) {
        User me = userService.getOrCreateCurrentUser();

        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));

        if (!isAdmin(me) && (p.getOrder() == null || p.getOrder().getUser() == null || !p.getOrder().getUser().getId().equals(me.getId()))) {
            throw new SecurityException("Forbidden: not your payment");
        }

        return paymentMapper.toResponse(p);
    }

    @Override
    public PaymentResponse update(Long id, PaymentUpdateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        if (!isAdmin(me) && (p.getOrder() == null || p.getOrder().getUser() == null || !p.getOrder().getUser().getId().equals(me.getId()))) {
            throw new SecurityException("Forbidden: not your payment");
        }

        if (p.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Payment already PAID");
        }

        if (req.transactionRef() != null) {
            p.setTransactionRef(req.transactionRef());
        }

        if (req.paymentStatus() != null) {
            p.setPaymentStatus(req.paymentStatus());

            Order order = p.getOrder();
            if (order != null) {
                if (req.paymentStatus() == PaymentStatus.PAID) {
                    order.setStatus(OrderStatus.PAID);
                } else if (req.paymentStatus() == PaymentStatus.FAILED) {
                    order.setStatus(OrderStatus.CANCELED);
                }
            }
        }

        Payment saved = paymentRepository.save(p);
        return paymentMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        if (p.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Cannot delete a PAID payment");
        }

        if (!isAdmin(me) && (p.getOrder() == null || p.getOrder().getUser() == null || !p.getOrder().getUser().getId().equals(me.getId()))) {
            throw new SecurityException("Forbidden: not your payment");
        }

        paymentRepository.delete(p);
    }

    private boolean isAdmin(User user) {
        return user != null && user.getUserType() == UserType.ADMIN;
    }
    private void restoreStock(Order order) {
        if (order == null || order.getItems() == null) {
            return;
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                continue;
            }

            if (product.getSaleType() == SaleMode.STANDARD) {
                Integer stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(stock + item.getQuantity());
                productRepository.save(product);

            } else if (product.getSaleType() == SaleMode.PREORDER) {
                Integer preorderCount = product.getPreorderCount() != null ? product.getPreorderCount() : 0;
                product.setPreorderCount(Math.max(0, preorderCount - item.getQuantity()));
                productRepository.save(product);
            }
        }
    }
}
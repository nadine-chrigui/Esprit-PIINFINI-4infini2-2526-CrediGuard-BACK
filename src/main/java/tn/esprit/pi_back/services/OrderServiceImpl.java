package tn.esprit.pi_back.services;




import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.order.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.repositories.OrderItemRepository;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.repositories.ProductRepository;
import tn.esprit.pi_back.repositories.PromoCodeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND = "Order not found: ";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PromoCodeRepository promoCodeRepository; // si tu l’as déjà
    private final UserService userService;

    @Override
    public OrderResponse create(OrderCreateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Order order = new Order();
        order.setUser(me);
        order.setStatus(OrderStatus.PENDING);

        PromoCode promo = null;

        if (req.promoCode() != null && !req.promoCode().isBlank()) {
            promo = promoCodeRepository.findByCodeIgnoreCase(req.promoCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Promo code not found"));
        }

        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        if (req.items() == null || req.items().isEmpty()) {
            throw new IllegalArgumentException("Order items are required.");
        }

        for (OrderItemCreateRequest it : req.items()) {
            Product product = productRepository.findById(it.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + it.productId()));

            Double unitPrice = (product.getCurrentPrice() != null)
                    ? product.getCurrentPrice()
                    : product.getBasePrice();

            if (unitPrice == null) {
                throw new IllegalStateException("Product price is null for product: " + product.getId());
            }

            int qty = it.quantity();
            double lineTotal = unitPrice * qty;

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(qty);
            oi.setUnitPrice(unitPrice);
            oi.setLineTotal(lineTotal);

            items.add(oi);
            total += lineTotal;
        }

        order.setItems(items);

        if (promo != null) {
            if (Boolean.FALSE.equals(promo.getActive())) {
                throw new IllegalArgumentException("Promo code is inactive");
            }

            if (promo.getMaxUses() != null
                    && promo.getUsedCount() != null
                    && promo.getUsedCount() >= promo.getMaxUses()) {
                throw new IllegalArgumentException("Promo code has reached max uses");
            }

            if (promo.getMinOrderAmount() != null && total < promo.getMinOrderAmount()) {
                throw new IllegalArgumentException("Order amount is below minimum required");
            }

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (promo.getStartAt() != null && now.isBefore(promo.getStartAt())) {
                throw new IllegalArgumentException("Promo code not started yet");
            }
            if (promo.getEndAt() != null && now.isAfter(promo.getEndAt())) {
                throw new IllegalArgumentException("Promo code expired");
            }

            double discount;
            if (promo.getDiscountType() == tn.esprit.pi_back.entities.enums.DiscountType.PERCENTAGE) {
                discount = total * (promo.getDiscountValue() / 100.0);
                if (promo.getMaxDiscountAmount() != null) {
                    discount = Math.min(discount, promo.getMaxDiscountAmount());
                }
            } else {
                discount = promo.getDiscountValue();
            }

            discount = Math.min(discount, total);
            total = total - discount;

            order.setPromoCode(promo);

            if (promo.getUsedCount() == null) {
                promo.setUsedCount(0);
            }
            promo.setUsedCount(promo.getUsedCount() + 1);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        // sécurité: propriétaire
        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order.");
        }

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMine() {
        User me = userService.getOrCreateCurrentUser();
        return orderRepository.findByUserId(me.getId()).stream().map(this::mapToResponse).toList();
    }

    @Override
    public OrderResponse update(Long id, OrderUpdateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order.");
        }

        if (req.status() != null) order.setStatus(req.status());

        if (req.financeReference() != null) order.setFinanceReference(req.financeReference());

        if (req.promoCodeId() != null) {
            PromoCode promo = promoCodeRepository.findById(req.promoCodeId())
                    .orElseThrow(() -> new IllegalArgumentException("PromoCode not found: " + req.promoCodeId()));
            order.setPromoCode(promo);
        }

        // (Optionnel) si tu veux recalculer total quand status change => pas obligatoire ici
        return mapToResponse(order);
    }

    @Override
    public void delete(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order.");
        }

        // hard delete (simple) :
        orderRepository.delete(order);

        // ou soft delete si tu ajoutes un champ "active" dans Order.
    }

    /* ================= MAPPER ================= */

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = (order.getItems() == null)
                ? List.of()
                : order.getItems().stream().map(this::mapItemToResponse).toList();

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getStatus(),
                order.getTotalAmount(),
                order.getPromoCode() != null ? order.getPromoCode().getId() : null,
                order.getFinanceReference(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    private OrderItemResponse mapItemToResponse(OrderItem it) {
        return new OrderItemResponse(
                it.getId(),
                it.getProduct() != null ? it.getProduct().getId() : null,
                it.getProduct() != null ? it.getProduct().getName() : null,
                it.getQuantity(),
                it.getUnitPrice(),
                it.getLineTotal()
        );
    }
    @Override
    @Transactional(readOnly = true)
    public Page<OrderAdminResponse> getAllAdmin(OrderStatus status, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Page<Order> orders;

        if (status != null && dateFrom != null && dateTo != null) {
            orders = orderRepository.findByStatusAndCreatedAtBetween(
                    status,
                    dateFrom.atStartOfDay(),
                    dateTo.atTime(23, 59, 59),
                    pageable
            );
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else if (dateFrom != null && dateTo != null) {
            orders = orderRepository.findByCreatedAtBetween(
                    dateFrom.atStartOfDay(),
                    dateTo.atTime(23, 59, 59),
                    pageable
            );
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::mapToAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getAdminById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        return mapToResponse(order);
    }

    @Override
    public OrderResponse updateStatusAdmin(Long id, OrderStatusUpdateRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        order.setStatus(req.status());

        return mapToResponse(order);
    }
    private OrderAdminResponse mapToAdminResponse(Order order) {
        String clientName = null;
        String clientEmail = null;

        if (order.getUser() != null) {
            clientName = order.getUser().getFullName();
            clientEmail = order.getUser().getEmail();
        }

        int itemCount = (order.getItems() != null) ? order.getItems().size() : 0;

        return new OrderAdminResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                clientName,
                clientEmail,
                order.getStatus(),
                order.getTotalAmount(),
                order.getPromoCode() != null ? order.getPromoCode().getId() : null,
                order.getFinanceReference(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemCount
        );
    }
}
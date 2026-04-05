package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.order.*;
import tn.esprit.pi_back.entities.Order;
import tn.esprit.pi_back.entities.OrderItem;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.PromoCode;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.DiscountType;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.entities.enums.SaleMode;
import tn.esprit.pi_back.mappers.OrderMapper;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.repositories.ProductRepository;
import tn.esprit.pi_back.repositories.PromoCodeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND = "Order not found: ";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse create(OrderCreateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        if (req.items() == null || req.items().isEmpty()) {
            throw new IllegalArgumentException("Order items are required.");
        }

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

        for (OrderItemCreateRequest it : req.items()) {
            Product product = productRepository.findById(it.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + it.productId()));

            Double unitPrice = product.getCurrentPrice() != null
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
            validatePromoCode(promo, total);

            double discount;
            if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
                discount = total * (promo.getDiscountValue() / 100.0);
                if (promo.getMaxDiscountAmount() != null) {
                    discount = Math.min(discount, promo.getMaxDiscountAmount());
                }
            } else {
                discount = promo.getDiscountValue();
            }

            discount = Math.min(discount, total);
            total -= discount;

            order.setPromoCode(promo);

            if (promo.getUsedCount() == null) {
                promo.setUsedCount(0);
            }
            promo.setUsedCount(promo.getUsedCount() + 1);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order.");
        }

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMine() {
        User me = userService.getOrCreateCurrentUser();
        return orderRepository.findByUserId(me.getId())
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse update(Long id, OrderUpdateRequest req) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order.");
        }

        if (req.status() != null) {
            order.setStatus(req.status());
        }

        if (req.financeReference() != null) {
            order.setFinanceReference(req.financeReference());
        }

        if (req.promoCodeId() != null) {
            PromoCode promo = promoCodeRepository.findById(req.promoCodeId())
                    .orElseThrow(() -> new IllegalArgumentException("PromoCode not found: " + req.promoCodeId()));
            order.setPromoCode(promo);
        }

        return orderMapper.toResponse(order);
    }

    @Override
    public void delete(Long id) {
        User me = userService.getOrCreateCurrentUser();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        if (!order.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Forbidden: not your order.");
        }

        orderRepository.delete(order);
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

        return orders.map(orderMapper::toAdminResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getAdminById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse updateStatusAdmin(Long id, OrderStatusUpdateRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND + id));

        if (req.status() == OrderStatus.PAID && order.getStatus() != OrderStatus.PAID) {
            decreaseStockForOrder(order);
        }

        order.setStatus(req.status());
        return orderMapper.toResponse(order);
    }

    private void decreaseStockForOrder(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalStateException("Order has no items.");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();

            if (product == null) {
                throw new IllegalStateException("Order item has no product.");
            }

            if (product.getSaleType() == SaleMode.STANDARD) {
                Integer currentStock = product.getStockQuantity();

                if (currentStock == null) {
                    throw new IllegalStateException("Stock is not defined for product: " + product.getName());
                }

                if (currentStock < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                }

                product.setStockQuantity(currentStock - item.getQuantity());
                productRepository.save(product);
            }

            if (product.getSaleType() == SaleMode.PREORDER) {
                Integer currentPreorderCount = product.getPreorderCount() != null
                        ? product.getPreorderCount()
                        : 0;

                product.setPreorderCount(currentPreorderCount + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private void validatePromoCode(PromoCode promo, double total) {
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

        LocalDateTime now = LocalDateTime.now();
        if (promo.getStartAt() != null && now.isBefore(promo.getStartAt())) {
            throw new IllegalArgumentException("Promo code not started yet");
        }
        if (promo.getEndAt() != null && now.isAfter(promo.getEndAt())) {
            throw new IllegalArgumentException("Promo code expired");
        }
    }
}
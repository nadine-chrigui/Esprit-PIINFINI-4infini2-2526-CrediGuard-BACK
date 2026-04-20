package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.checkout.CheckoutRequest;
import tn.esprit.pi_back.dto.checkout.CheckoutResponse;
import tn.esprit.pi_back.dto.delivery.DeliveryResponse;
import tn.esprit.pi_back.dto.order.OrderResponse;
import tn.esprit.pi_back.dto.promotion.ProductPriceView;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.*;
import tn.esprit.pi_back.mappers.OrderMapper;
import tn.esprit.pi_back.mappers.PaymentMapper;
import tn.esprit.pi_back.repositories.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final PromoCodeRepository promoCodeRepository;

    private final UserService userService;
    private final PromotionService promotionService;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final DeliveryService deliveryService;

    @Override
    public CheckoutResponse checkout(CheckoutRequest req) {
        User me = userService.getCurrentUserOrThrow();

        Cart cart = cartRepository.findByUserIdAndStatus(me.getId(), CartStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Active cart not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        DeliveryAddress address = deliveryAddressRepository.findById(req.addressId())
                .orElseThrow(() -> new IllegalArgumentException("DeliveryAddress not found: " + req.addressId()));

        PromoCode promo = null;
        if (req.promoCode() != null && !req.promoCode().isBlank()) {
            promo = promoCodeRepository.findByCodeIgnoreCase(req.promoCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Promo code not found"));
        }

        double cartSubtotal = cart.getItems().stream()
                .mapToDouble(item -> {
                    Product product = item.getProduct();

                    Double basePrice = product.getCurrentPrice() != null
                            ? product.getCurrentPrice()
                            : product.getBasePrice();

                    if (basePrice == null) {
                        throw new IllegalStateException("Product price is null for product: " + product.getId());
                    }

                    return basePrice * item.getQuantity();
                })
                .sum();

        Order order = new Order();
        order.setUser(me);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product == null) {
                throw new IllegalStateException("Cart item product is null");
            }

            if (!product.isActive()) {
                throw new IllegalStateException("Product is inactive: " + product.getName());
            }

            validateStockForCheckout(product, cartItem.getQuantity());

            ProductPriceView priceView = promotionService.calculateProductPrice(product, cartSubtotal);

            double unitPrice = priceView.finalPrice();
            int qty = cartItem.getQuantity();
            double lineTotal = unitPrice * qty;

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(qty);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(lineTotal);

            orderItems.add(orderItem);
            total += lineTotal;
        }

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

        order.setItems(orderItems);
        double deliveryFee = calculateDeliveryFee(req.deliveryType());
        double finalTotal = total + deliveryFee;

        order.setTotalAmount(round(finalTotal));

        Order savedOrder = orderRepository.save(order);

        Delivery delivery = new Delivery();
        delivery.setOrder(savedOrder);
        delivery.setAddress(address);
        delivery.setDeliveryType(req.deliveryType());
        delivery.setDeliveryStatus(DeliveryStatus.PENDING);
        delivery.setDeliverySlot(req.deliverySlot());
        delivery.setScheduledAt(req.scheduledAt());
        delivery.setDeliveryFee(calculateDeliveryFee(req.deliveryType()));

        Delivery savedDelivery = deliveryRepository.save(delivery);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentType(req.paymentType());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setAmount(savedOrder.getTotalAmount());

        Payment savedPayment = paymentRepository.save(payment);

        reserveStock(savedOrder);

        cart.getItems().clear();
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        OrderResponse orderResponse = orderMapper.toResponse(savedOrder);
        DeliveryResponse deliveryResponse = deliveryService.getById(savedDelivery.getId());
        PaymentResponse paymentResponse = paymentMapper.toResponse(savedPayment);

        return new CheckoutResponse(orderResponse, deliveryResponse, paymentResponse);
    }

    private void validateStockForCheckout(Product product, int quantity) {
        if (product.getSaleType() == SaleMode.STANDARD) {
            Integer currentStock = product.getStockQuantity();

            if (currentStock == null) {
                throw new IllegalStateException("Stock is not defined for product: " + product.getName());
            }

            if (currentStock < quantity) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
        }
    }

    private void reserveStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();

            if (product.getSaleType() == SaleMode.STANDARD) {
                Integer currentStock = product.getStockQuantity();

                if (currentStock == null || currentStock < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                }

                product.setStockQuantity(currentStock - item.getQuantity());
                productRepository.save(product);

            } else if (product.getSaleType() == SaleMode.PREORDER) {
                Integer preorderCount = product.getPreorderCount() != null
                        ? product.getPreorderCount()
                        : 0;

                product.setPreorderCount(preorderCount + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private double calculateDeliveryFee(DeliveryType deliveryType) {
        if (deliveryType == null) {
            return 0.0;
        }

        return switch (deliveryType) {
            case STANDARD -> 8.0;
            case EXPRESS -> 15.0;
        };
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
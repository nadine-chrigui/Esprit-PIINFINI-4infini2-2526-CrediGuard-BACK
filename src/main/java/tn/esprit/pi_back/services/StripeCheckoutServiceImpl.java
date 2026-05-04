package tn.esprit.pi_back.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.stripe.StripeCheckoutResponse;
import tn.esprit.pi_back.entities.Order;
import tn.esprit.pi_back.entities.Payment;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.entities.enums.PaymentStatus;
import tn.esprit.pi_back.entities.enums.PaymentType;
import tn.esprit.pi_back.mappers.PaymentMapper;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.repositories.PaymentRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class StripeCheckoutServiceImpl implements StripeCheckoutService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.currency:usd}")
    private String stripeCurrency;

    @Value("${stripe.success-url:http://localhost:4200/front/payment-success?session_id={CHECKOUT_SESSION_ID}}")
    private String successUrl;

    @Value("${stripe.cancel-url:http://localhost:4200/front/payment-cancel}")
    private String cancelUrl;

    @Override
    public StripeCheckoutResponse createCheckoutSession(Long paymentId) {
        ensureStripeConfigured();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getPaymentType() != PaymentType.CARD) {
            throw new IllegalStateException("Stripe checkout is only available for CARD payments.");
        }

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Payment is already paid.");
        }

        Stripe.apiKey = normalizedStripeSecretKey();
        String currency = normalizedStripeCurrency();

        long amountInMinorUnit = Math.round(payment.getAmount() * 100);
        Order order = payment.getOrder();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(String.valueOf(payment.getId()))
                .putMetadata("paymentId", String.valueOf(payment.getId()))
                .putMetadata("orderId", order != null ? String.valueOf(order.getId()) : "")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amountInMinorUnit)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("CrediGuard Order #" + (order != null ? order.getId() : payment.getId()))
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            Session session = Session.create(params);
            payment.setStripeSessionId(session.getId());
            payment.setTransactionRef(session.getId());
            paymentRepository.save(payment);
            return new StripeCheckoutResponse(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to create Stripe checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse confirmCheckoutSession(String sessionId) {
        ensureStripeConfigured();
        Stripe.apiKey = normalizedStripeSecretKey();

        try {
            Session session = Session.retrieve(sessionId);
            Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for Stripe session: " + sessionId));

            if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setTransactionRef(session.getId());
                payment.setStripePaymentIntentId(session.getPaymentIntent());

                Order order = payment.getOrder();
                if (order != null && order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);
                }
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
            }

            return paymentMapper.toResponse(paymentRepository.save(payment));
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to confirm Stripe checkout session: " + e.getMessage(), e);
        }
    }

    private void ensureStripeConfigured() {
        if (normalizedStripeSecretKey().isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured. Set STRIPE_SECRET_KEY or stripe.secret-key.");
        }
    }

    private String normalizedStripeSecretKey() {
        return normalizeConfigValue(stripeSecretKey);
    }

    private String normalizedStripeCurrency() {
        String currency = normalizeConfigValue(stripeCurrency);
        return currency.isBlank() ? "usd" : currency.toLowerCase(Locale.ROOT);
    }

    private String normalizeConfigValue(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();

        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'"))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        return normalized;
    }
}

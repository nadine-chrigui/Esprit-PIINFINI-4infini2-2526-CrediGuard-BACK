package tn.esprit.pi_back.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.flouci.FlouciPaymentResponse;
import tn.esprit.pi_back.entities.Order;
import tn.esprit.pi_back.entities.Payment;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.entities.enums.PaymentStatus;
import tn.esprit.pi_back.entities.enums.PaymentType;
import tn.esprit.pi_back.mappers.PaymentMapper;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.repositories.PaymentRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "flouci.enabled", havingValue = "true")
public class FlouciPaymentServiceImpl implements FlouciPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${flouci.public-key:}")
    private String publicKey;

    @Value("${flouci.secret-key:}")
    private String secretKey;

    @Value("${flouci.api-base-url:https://developers.flouci.com/api/v2}")
    private String apiBaseUrl;

    @Value("${flouci.success-url:http://localhost:4200/front/flouci-payment-success}")
    private String successUrl;

    @Value("${flouci.fail-url:http://localhost:4200/front/flouci-payment-cancel}")
    private String failUrl;

    @Value("${flouci.accept-card:true}")
    private boolean acceptCard;

    @Override
    public FlouciPaymentResponse generatePayment(Long paymentId) {
        ensureConfigured();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getPaymentType() != PaymentType.CARD && payment.getPaymentType() != PaymentType.WALLET) {
            throw new IllegalStateException("Flouci payment is only available for CARD or WALLET payments.");
        }

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Payment is already paid.");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("amount", String.valueOf(toMillimes(payment.getAmount())));
        body.put("accept_card", acceptCard);
        body.put("session_timeout_secs", 1200);
        body.put("success_link", successUrl + "?paymentId=" + payment.getId());
        body.put("fail_link", failUrl + "?paymentId=" + payment.getId());
        body.put("developer_tracking_id", "crediguard-payment-" + payment.getId());

        JsonNode response = postJson("/generate_payment", body);
        JsonNode result = response.path("result");

        String flouciPaymentId = result.path("payment_id").asText(null);
        String paymentLink = result.path("link").asText(null);

        if (flouciPaymentId == null || paymentLink == null) {
            throw new IllegalStateException("Invalid Flouci generate payment response.");
        }

        payment.setFlouciPaymentId(flouciPaymentId);
        payment.setTransactionRef(flouciPaymentId);
        paymentRepository.save(payment);

        return new FlouciPaymentResponse(payment.getId(), flouciPaymentId, paymentLink);
    }

    @Override
    public PaymentResponse verifyPayment(Long paymentId) {
        ensureConfigured();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getFlouciPaymentId() == null || payment.getFlouciPaymentId().isBlank()) {
            throw new IllegalStateException("Payment has no Flouci payment id.");
        }

        JsonNode response = getJson("/verify_payment/" + payment.getFlouciPaymentId());
        boolean paid = response.path("success").asBoolean(false)
                || response.path("result").path("status").asText("").equalsIgnoreCase("SUCCESS")
                || response.path("result").path("status").asText("").equalsIgnoreCase("PAID");

        if (paid) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setTransactionRef(payment.getFlouciPaymentId());

            Order order = payment.getOrder();
            if (order != null && order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            }
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    private JsonNode postJson(String path, Map<String, Object> body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl + path))
                    .header("Authorization", authorizationHeader())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Flouci API error " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call Flouci API.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Flouci API call interrupted.", e);
        }
    }

    private JsonNode getJson(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl + path))
                    .header("Authorization", authorizationHeader())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Flouci API error " + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call Flouci API.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Flouci API call interrupted.", e);
        }
    }

    private int toMillimes(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalStateException("Payment amount must be positive.");
        }
        return (int) Math.round(amount * 1000);
    }

    private void ensureConfigured() {
        if (publicKey == null || publicKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Flouci keys are not configured. Set FLOUCI_PUBLIC_KEY and FLOUCI_SECRET_KEY.");
        }
    }

    private String authorizationHeader() {
        return "Bearer " + publicKey + ":" + secretKey;
    }
}

package tn.esprit.pi_back.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentIntentRequest;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentIntentResponse;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentResponse;
import tn.esprit.pi_back.entities.CrowdfundingProject;
import tn.esprit.pi_back.entities.InvestmentPayment;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.InvestmentPaymentStatus;
import tn.esprit.pi_back.events.InvestmentPaymentConfirmedEvent;
import tn.esprit.pi_back.repositories.CrowdfundingProjectRepository;
import tn.esprit.pi_back.repositories.InvestmentPaymentRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class InvestmentPaymentServiceImpl implements InvestmentPaymentService {

    private final InvestmentPaymentRepository investmentPaymentRepository;
    private final UserRepository userRepository;
    private final CrowdfundingProjectRepository projectRepository;
    private final ReturnPaymentService returnPaymentService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.publishable-key:}")
    private String stripePublishableKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${stripe.currency:usd}")
    private String stripeCurrency;

    @PostConstruct
    void initStripe() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    @Override
    public InvestmentPaymentIntentResponse createPaymentIntent(InvestmentPaymentIntentRequest request) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }

        User investor = userRepository.findById(request.investorId())
                .orElseThrow(() -> new IllegalArgumentException("Investor not found"));

        CrowdfundingProject project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (project.getStatus() == CrowdfundingProject.ProjectStatus.CLOSED) {
            throw new IllegalStateException("Closed projects cannot receive investments");
        }

        double expectedReturn = calculateExpectedReturn(request.amount(), project.getInterestRate(), request.durationYears());

        InvestmentPayment payment = new InvestmentPayment();
        payment.setInvestor(investor);
        payment.setProject(project);
        payment.setAmount(round(request.amount()));
        payment.setCurrency(normalizeCurrency(stripeCurrency));
        payment.setDurationYears(request.durationYears());
        payment.setScheduleFrequency(request.scheduleFrequency());
        payment.setInterestRateSnapshot(project.getInterestRate());
        payment.setExpectedReturnSnapshot(round(expectedReturn));
        payment.setStatus(InvestmentPaymentStatus.PENDING);
        payment = investmentPaymentRepository.save(payment);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toStripeAmount(payment.getAmount()))
                    .setCurrency(payment.getCurrency())
                    .addPaymentMethodType("card")
                    .putMetadata("investmentPaymentId", String.valueOf(payment.getId()))
                    .putMetadata("investorId", String.valueOf(investor.getId()))
                    .putMetadata("projectId", String.valueOf(project.getProjectId()))
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setStripeClientSecret(paymentIntent.getClientSecret());

            investmentPaymentRepository.save(payment);

            return new InvestmentPaymentIntentResponse(
                    payment.getId(),
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    stripePublishableKey,
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getExpectedReturnSnapshot()
            );
        } catch (StripeException ex) {
            String detail = ex.getMessage() != null && !ex.getMessage().isBlank()
                    ? ex.getMessage()
                    : "Unknown Stripe error";
            throw new IllegalStateException("Unable to create Stripe PaymentIntent: " + detail, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentPaymentResponse getById(Long id) {
        return toResponse(investmentPaymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment payment not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentPaymentResponse> getByInvestor(Long investorId) {
        return investmentPaymentRepository.findByInvestorIdOrderByCreatedAtDesc(investorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void handleWebhook(String payload, String stripeSignature) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }

        try {
            Event event = Webhook.constructEvent(payload, stripeSignature, stripeWebhookSecret);
            String paymentIntentId = extractPaymentIntentId(payload);
            if (paymentIntentId == null || paymentIntentId.isBlank()) {
                log.info("Ignoring Stripe event {} of type {} because no payment_intent id was found",
                        event.getId(), event.getType());
                return;
            }

            if (handleInvestmentPaymentEvent(paymentIntentId, event.getType())) {
                return;
            }

            returnPaymentService.handleStripeWebhookEvent(paymentIntentId, event.getType());
        } catch (SignatureVerificationException ex) {
            throw new SecurityException("Invalid Stripe signature", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to process Stripe webhook", ex);
        }
    }

    private boolean handleInvestmentPaymentEvent(String paymentIntentId, String eventType) {
        InvestmentPayment payment = investmentPaymentRepository.findByStripePaymentIntentId(paymentIntentId).orElse(null);
        if (payment == null) {
            return false;
        }

        if ("payment_intent.succeeded".equals(eventType)) {
            markSucceeded(payment);
        } else if ("payment_intent.payment_failed".equals(eventType)) {
            markFailed(payment);
        } else if ("payment_intent.canceled".equals(eventType)) {
            markCanceled(payment);
        }

        return true;
    }

    private void markSucceeded(InvestmentPayment payment) {
        if (payment.getStatus() == InvestmentPaymentStatus.SUCCEEDED) {
            return;
        }

        payment.setStatus(InvestmentPaymentStatus.SUCCEEDED);
        payment.setConfirmedAt(LocalDateTime.now());
        investmentPaymentRepository.save(payment);
        log.info("Stripe payment {} marked as SUCCEEDED for investment payment {}",
                payment.getStripePaymentIntentId(), payment.getId());

        eventPublisher.publishEvent(new InvestmentPaymentConfirmedEvent(payment.getId()));
    }

    private void markFailed(InvestmentPayment payment) {
        payment.setStatus(InvestmentPaymentStatus.FAILED);
        investmentPaymentRepository.save(payment);
    }

    private void markCanceled(InvestmentPayment payment) {
        payment.setStatus(InvestmentPaymentStatus.CANCELED);
        investmentPaymentRepository.save(payment);
    }

    private String extractPaymentIntentId(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        JsonNode objectNode = root.path("data").path("object");
        if (!objectNode.isObject()) {
            return null;
        }
        JsonNode idNode = objectNode.get("id");
        return idNode != null && !idNode.isNull() ? idNode.asText() : null;
    }

    private InvestmentPaymentResponse toResponse(InvestmentPayment payment) {
        return new InvestmentPaymentResponse(
                payment.getId(),
                payment.getInvestor().getId(),
                payment.getProject().getProjectId(),
                payment.getInvestment() != null ? payment.getInvestment().getInvestmentId() : null,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getDurationYears(),
                payment.getScheduleFrequency(),
                payment.getInterestRateSnapshot(),
                payment.getExpectedReturnSnapshot(),
                payment.getStripePaymentIntentId(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getConfirmedAt()
        );
    }

    private long toStripeAmount(Double amount) {
        return Math.round(amount * 100);
    }

    private double calculateExpectedReturn(Double amount, Double annualRate, Integer years) {
        return amount * Math.pow(1 + (annualRate / 100.0), years);
    }

    private double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "usd";
        }
        return currency.trim().toLowerCase();
    }
}

package tn.esprit.pi_back.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.ReturnPayment.*;
import tn.esprit.pi_back.entities.Investment;
import tn.esprit.pi_back.entities.ReturnPayment;
import tn.esprit.pi_back.repositories.InvestmentRepository;
import tn.esprit.pi_back.repositories.ReturnPaymentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReturnPaymentServiceImpl implements ReturnPaymentService {

    private final ReturnPaymentRepository returnPaymentRepository;
    private final InvestmentRepository investmentRepository;
    private final InvestorAnalyticsService investorAnalyticsService;
    private final GoogleCalendarService googleCalendarService;

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.publishable-key:}")
    private String stripePublishableKey;

    @Value("${stripe.currency:usd}")
    private String stripeCurrency;

    @PostConstruct
    void initStripe() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    @Override
    public ReturnPaymentResponse create(ReturnPaymentCreateRequest req) {
        Investment investment = investmentRepository.findById(req.investmentId())
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        ReturnPayment rp = new ReturnPayment();
        rp.setAmount(round(req.amount()));
        rp.setPaymentDate(req.paymentDate());
        rp.setType(req.type());
        rp.setInvestment(investment);
        rp.setCurrency(normalizeCurrency(stripeCurrency));

        return map(returnPaymentRepository.save(rp));
    }

    @Override
    public ReturnPaymentIntentResponse createPaymentIntent(ReturnPaymentIntentRequest req) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }

        ReturnPayment rp = returnPaymentRepository.findById(req.returnPaymentId())
                .orElseThrow(() -> new RuntimeException("Return payment not found"));
        Investment investment = rp.getInvestment();

        if (rp.getStatus() == ReturnPayment.ReturnStatus.PAID) {
            throw new IllegalStateException("This scheduled return payment is already paid");
        }

        if (rp.getStatus() == ReturnPayment.ReturnStatus.FAILED) {
            rp.setStatus(ReturnPayment.ReturnStatus.SCHEDULED);
        }

        rp.setCurrency(normalizeCurrency(rp.getCurrency() == null || rp.getCurrency().isBlank()
                ? stripeCurrency
                : rp.getCurrency()));
        rp = returnPaymentRepository.save(rp);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toStripeAmount(rp.getAmount()))
                    .setCurrency(rp.getCurrency())
                    .addPaymentMethodType("card")
                    .putMetadata("returnPaymentId", String.valueOf(rp.getReturnId()))
                    .putMetadata("investmentId", String.valueOf(investment.getInvestmentId()))
                    .putMetadata("flow", "return_payment")
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            rp.setStripePaymentIntentId(paymentIntent.getId());
            returnPaymentRepository.save(rp);

            return new ReturnPaymentIntentResponse(
                    rp.getReturnId(),
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    stripePublishableKey,
                    rp.getAmount(),
                    rp.getCurrency()
            );
        } catch (StripeException ex) {
            String detail = ex.getMessage() != null && !ex.getMessage().isBlank()
                    ? ex.getMessage()
                    : "Unknown Stripe error";
            throw new IllegalStateException("Unable to create Stripe PaymentIntent for return payment: " + detail, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnPaymentResponse> getAll() {
        return returnPaymentRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnPaymentResponse getById(Long id) {
        return map(returnPaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReturnPayment not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnPaymentResponse> getByInvestment(Long investmentId) {
        return returnPaymentRepository.findByInvestmentInvestmentId(investmentId)
                .stream().map(this::map).toList();
    }

    @Override
    public ReturnPaymentResponse update(Long id, ReturnPaymentUpdateRequest req) {
        ReturnPayment rp = returnPaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ReturnPayment not found"));
        ReturnPayment.ReturnStatus previousStatus = rp.getStatus();

        if (req.status() != null) {
            rp.setStatus(req.status());
        }

        if (previousStatus != ReturnPayment.ReturnStatus.PAID && rp.getStatus() == ReturnPayment.ReturnStatus.PAID) {
            rp.setConfirmedAt(LocalDateTime.now());
            safelyRefreshAnalytics(rp);
        }

        if (previousStatus != rp.getStatus()) {
            safelySyncGoogleCalendar(rp);
        }

        return map(rp);
    }

    @Override
    public boolean handleStripeWebhookEvent(String paymentIntentId, String eventType) {
        ReturnPayment rp = returnPaymentRepository.findByStripePaymentIntentId(paymentIntentId).orElse(null);
        if (rp == null) {
            return false;
        }

        switch (eventType) {
            case "payment_intent.succeeded" -> markPaid(rp);
            case "payment_intent.payment_failed", "payment_intent.canceled" -> markFailed(rp);
            default -> {
                return true;
            }
        }

        return true;
    }

    @Override
    public void delete(Long id) {
        returnPaymentRepository.deleteById(id);
    }

    private ReturnPaymentResponse map(ReturnPayment rp) {
        return new ReturnPaymentResponse(
                rp.getReturnId(),
                rp.getAmount(),
                rp.getPaymentDate(),
                rp.getType().name(),
                rp.getStatus().name(),
                rp.getInvestment().getInvestmentId(),
                rp.getCurrency(),
                rp.getStripePaymentIntentId(),
                rp.getConfirmedAt(),
                rp.getGoogleCalendarEventLink(),
                rp.getGoogleCalendarSyncedAt()
        );
    }

    private void markPaid(ReturnPayment rp) {
        if (rp.getStatus() == ReturnPayment.ReturnStatus.PAID) {
            return;
        }

        rp.setStatus(ReturnPayment.ReturnStatus.PAID);
        rp.setConfirmedAt(LocalDateTime.now());
        returnPaymentRepository.save(rp);
        safelyRefreshAnalytics(rp);
        safelySyncGoogleCalendar(rp);
    }

    private void markFailed(ReturnPayment rp) {
        if (rp.getStatus() == ReturnPayment.ReturnStatus.FAILED) {
            return;
        }

        rp.setStatus(ReturnPayment.ReturnStatus.FAILED);
        returnPaymentRepository.save(rp);
        safelySyncGoogleCalendar(rp);
    }

    private long toStripeAmount(Double amount) {
        return Math.round(amount * 100);
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

    private void safelyRefreshAnalytics(ReturnPayment rp) {
        try {
            investorAnalyticsService.generateSnapshot(rp.getInvestment());
        } catch (RuntimeException ex) {
            log.warn("Unable to refresh investor analytics after return payment {}", rp.getReturnId(), ex);
        }
    }

    private void safelySyncGoogleCalendar(ReturnPayment rp) {
        try {
            googleCalendarService.syncReturnPaymentStatus(rp.getReturnId());
        } catch (RuntimeException ex) {
            log.warn("Unable to sync Google Calendar after return payment status change {}", rp.getReturnId(), ex);
        }
    }
}

package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.ReturnPayment.*;
import java.util.List;

public interface ReturnPaymentService {
    ReturnPaymentResponse create(ReturnPaymentCreateRequest req);
    ReturnPaymentIntentResponse createPaymentIntent(ReturnPaymentIntentRequest req);
    List<ReturnPaymentResponse> getAll();
    ReturnPaymentResponse getById(Long id);
    List<ReturnPaymentResponse> getByInvestment(Long investmentId);
    ReturnPaymentResponse update(Long id, ReturnPaymentUpdateRequest req);
    boolean handleStripeWebhookEvent(String paymentIntentId, String eventType);
    void delete(Long id);
}

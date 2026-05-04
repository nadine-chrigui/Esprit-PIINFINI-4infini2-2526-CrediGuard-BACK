package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentIntentRequest;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentIntentResponse;
import tn.esprit.pi_back.dto.InvestmentPayment.InvestmentPaymentResponse;

import java.util.List;

public interface InvestmentPaymentService {
    InvestmentPaymentIntentResponse createPaymentIntent(InvestmentPaymentIntentRequest request);
    InvestmentPaymentResponse getById(Long id);
    List<InvestmentPaymentResponse> getByInvestor(Long investorId);
    void handleWebhook(String payload, String stripeSignature);
}

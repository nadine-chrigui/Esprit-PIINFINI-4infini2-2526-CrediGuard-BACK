package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.flouci.FlouciPaymentResponse;

public interface FlouciPaymentService {
    FlouciPaymentResponse generatePayment(Long paymentId);
    PaymentResponse verifyPayment(Long paymentId);
}

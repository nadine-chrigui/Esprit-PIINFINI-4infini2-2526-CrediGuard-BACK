package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Payment.PaymentCreateRequest;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.Payment.PaymentUpdateRequest;

import java.util.List;

public interface PaymentService {
    PaymentResponse create(PaymentCreateRequest req);
    List<PaymentResponse> getAll();
    PaymentResponse getById(Long id);
    PaymentResponse getByOrder(Long orderId);
    PaymentResponse update(Long id, PaymentUpdateRequest req);
    void delete(Long id);
}
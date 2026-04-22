package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.entities.Payment;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder() != null ? p.getOrder().getId() : null,
                p.getAmount(),
                p.getPaymentType(),
                p.getPaymentStatus(),
                p.getTransactionRef(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
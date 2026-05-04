package tn.esprit.pi_back.dto.ReturnPayment;

import tn.esprit.pi_back.entities.ReturnPayment;

public record ReturnPaymentUpdateRequest(
        ReturnPayment.ReturnStatus status
) {}

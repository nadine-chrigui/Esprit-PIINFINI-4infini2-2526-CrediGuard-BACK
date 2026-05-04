package tn.esprit.pi_back.dto.ReturnPayment;

import jakarta.validation.constraints.*;
import tn.esprit.pi_back.entities.ReturnPayment;

import java.time.LocalDate;

public record ReturnPaymentCreateRequest(

        @NotNull @Positive
        Double amount,

        @NotNull
        LocalDate paymentDate,

        @NotNull
        ReturnPayment.ReturnType type,

        @NotNull
        Long investmentId

) {}

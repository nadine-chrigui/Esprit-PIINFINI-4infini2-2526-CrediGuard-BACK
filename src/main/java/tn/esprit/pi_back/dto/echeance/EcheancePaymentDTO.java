package tn.esprit.pi_back.dto.echeance;

import jakarta.validation.constraints.NotNull;

public record EcheancePaymentDTO(
        @NotNull
        Double montantPaye
) {}
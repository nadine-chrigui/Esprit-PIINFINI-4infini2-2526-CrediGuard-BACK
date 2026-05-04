package tn.esprit.pi_back.dto.Investment;

import java.time.LocalDate;

public record InvestmentResponse(
        Long investmentId,
        Double amount,
        LocalDate investmentDate,
        Double expectedReturn,
        String status,
        Long investorId,
        Long projectId
) {}

package tn.esprit.pi_back.dto.Investment;

import tn.esprit.pi_back.entities.Investment;

public record InvestmentUpdateRequest(
        Investment.InvestmentStatus status
) {}

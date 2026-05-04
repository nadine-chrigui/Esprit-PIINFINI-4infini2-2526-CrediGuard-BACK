package tn.esprit.pi_back.dto.InvestorAnalytics;

import tn.esprit.pi_back.entities.enums.InvestorClassification;

import java.time.LocalDateTime;

public record InvestorAnalyticsResponse(
        Long id,
        Long investorId,
        Long projectId,
        Long investmentId,
        Double totalInvested,
        Double averageInvestment,
        Double totalReturnsReceived,
        Double roi,
        Long fundedProjectsCount,
        InvestorClassification classification,
        LocalDateTime createdAt
) {}

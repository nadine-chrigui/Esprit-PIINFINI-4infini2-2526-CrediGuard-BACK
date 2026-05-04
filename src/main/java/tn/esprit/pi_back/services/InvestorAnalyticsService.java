package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.InvestorAnalytics.InvestorAnalyticsResponse;
import tn.esprit.pi_back.entities.Investment;
import tn.esprit.pi_back.entities.InvestorAnalyticsSnapshot;

import java.util.List;

public interface InvestorAnalyticsService {
    InvestorAnalyticsSnapshot generateSnapshot(Investment investment);
    InvestorAnalyticsResponse getLatestByInvestor(Long investorId);
    List<InvestorAnalyticsResponse> getHistoryByInvestor(Long investorId);
}

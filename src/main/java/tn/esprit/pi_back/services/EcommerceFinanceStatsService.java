package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.finance.*;

import java.util.List;

public interface EcommerceFinanceStatsService {
    EcommerceFinanceOverviewResponse getOverview();
    List<RevenueByMonthResponse> getRevenueByMonth();
    List<PaymentMethodStatsResponse> getPaymentMethodDistribution();
    List<TopProductStatsResponse> getTopProducts();
    List<LowStockProductResponse> getLowStockProducts();
    List<RevenueByCategoryResponse> getRevenueByCategory();
}
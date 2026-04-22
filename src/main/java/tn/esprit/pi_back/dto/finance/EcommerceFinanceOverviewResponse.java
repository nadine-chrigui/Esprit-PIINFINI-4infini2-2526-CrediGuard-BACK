package tn.esprit.pi_back.dto.finance;

public record EcommerceFinanceOverviewResponse(
        double totalRevenue,
        double monthlyRevenue,
        long paidOrders,
        double averageOrderValue
) {
}
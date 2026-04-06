package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceSummaryResponse {

    private long totalTransactions;
    private double totalRevenue;
    private double totalExpenses;
    private long totalAccounts;
    private long pendingTransactions;
    private long totalRemboursements;

    // USER STATS
    private long totalUsers;
    private long totalAdmins;
    private long totalBeneficiaries;
    private long totalPartners;

    // CREDIT STATS
    private long totalCredits;
    private long activeCredits;
    private long closedCredits;
    private double totalAmountGranted;
    private double totalAmountRemaining;

    // ADVANCED ANALYTICS
    private double revenueTrend; // Percentage vs last month
    private double expenseTrend; // Percentage vs last month
    private java.util.Map<String, Double> monthlyRevenue;
    private java.util.Map<String, Double> monthlyExpenses;
    private java.util.List<String> activeAlerts;
    private double forecastedRevenue;
}

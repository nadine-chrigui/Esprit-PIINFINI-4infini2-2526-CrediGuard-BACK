package tn.esprit.pi_back.dto.finance;

public record RevenueByMonthResponse(
        String month,
        double revenue
) {
}
package tn.esprit.pi_back.controllers;
import tn.esprit.pi_back.dto.finance.RevenueByCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.finance.EcommerceFinanceOverviewResponse;
import tn.esprit.pi_back.dto.finance.PaymentMethodStatsResponse;
import tn.esprit.pi_back.dto.finance.RevenueByMonthResponse;
import tn.esprit.pi_back.dto.finance.TopProductStatsResponse;
import tn.esprit.pi_back.services.EcommerceFinanceStatsService;
import tn.esprit.pi_back.dto.finance.LowStockProductResponse;
import java.util.List;

@RestController
@RequestMapping("/finance/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EcommerceFinanceStatsController {

    private final EcommerceFinanceStatsService ecommerceFinanceStatsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EcommerceFinanceOverviewResponse> getOverview() {
        return ResponseEntity.ok(ecommerceFinanceStatsService.getOverview());
    }
    @GetMapping("/revenue-by-month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenueByMonthResponse>> getRevenueByMonth() {
        return ResponseEntity.ok(ecommerceFinanceStatsService.getRevenueByMonth());
    }

    @GetMapping("/payment-method-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentMethodStatsResponse>> getPaymentMethodDistribution() {
        return ResponseEntity.ok(ecommerceFinanceStatsService.getPaymentMethodDistribution());
    }
    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopProductStatsResponse>> getTopProducts() {
        return ResponseEntity.ok(ecommerceFinanceStatsService.getTopProducts());
    }
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LowStockProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(ecommerceFinanceStatsService.getLowStockProducts());
    }
    @GetMapping("/revenue-by-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenueByCategoryResponse>> getRevenueByCategory() {
        return ResponseEntity.ok(ecommerceFinanceStatsService.getRevenueByCategory());
    }
}
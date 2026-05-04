package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.finance.RevenueByCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.finance.EcommerceFinanceOverviewResponse;
import tn.esprit.pi_back.dto.finance.PaymentMethodStatsResponse;
import tn.esprit.pi_back.dto.finance.TopProductStatsResponse;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.repositories.OrderItemRepository;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.dto.finance.RevenueByMonthResponse;
import tn.esprit.pi_back.repositories.PaymentRepository;
import tn.esprit.pi_back.dto.finance.LowStockProductResponse;
import java.time.Month;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import tn.esprit.pi_back.dto.finance.LowStockProductResponse;
import tn.esprit.pi_back.repositories.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EcommerceFinanceStatsServiceImpl implements EcommerceFinanceStatsService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    @Override
    public EcommerceFinanceOverviewResponse getOverview() {
        List<OrderStatus> paidStatuses = List.of(
                OrderStatus.PAID,
                OrderStatus.DELIVERED
        );

        Double totalRevenue = orderRepository.sumRevenueByStatuses(paidStatuses);
        Long paidOrders = orderRepository.countByStatuses(paidStatuses);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        Double monthlyRevenue = orderRepository.sumRevenueByStatusesAndDateRange(
                paidStatuses,
                startOfMonth,
                endOfMonth
        );

        double totalRevenueValue = totalRevenue != null ? totalRevenue : 0.0;
        double monthlyRevenueValue = monthlyRevenue != null ? monthlyRevenue : 0.0;
        long paidOrdersValue = paidOrders != null ? paidOrders : 0L;
        double averageOrderValue = paidOrdersValue > 0 ? totalRevenueValue / paidOrdersValue : 0.0;

        return new EcommerceFinanceOverviewResponse(
                totalRevenueValue,
                monthlyRevenueValue,
                paidOrdersValue,
                averageOrderValue
        );
    }
    @Override
    public List<RevenueByMonthResponse> getRevenueByMonth() {
        List<OrderStatus> paidStatuses = List.of(
                OrderStatus.PAID,
                OrderStatus.DELIVERED
        );

        return orderRepository.sumRevenueGroupedByMonth(paidStatuses)
                .stream()
                .map(row -> {
                    Integer year = (Integer) row[0];
                    Integer month = (Integer) row[1];
                    Double revenue = ((Number) row[2]).doubleValue();

                    String label = Month.of(month).name() + " " + year;
                    return new RevenueByMonthResponse(label, revenue);
                })
                .toList();
    }
    @Override
    public List<PaymentMethodStatsResponse> getPaymentMethodDistribution() {
        return paymentRepository.countAndSumByPaymentType()
                .stream()
                .map(row -> new PaymentMethodStatsResponse(
                        String.valueOf(row[0]),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue()
                ))
                .toList();
    }

    @Override
    public List<TopProductStatsResponse> getTopProducts() {
        List<OrderStatus> paidStatuses = List.of(
                OrderStatus.PAID,
                OrderStatus.DELIVERED
        );

        return orderItemRepository.findTopProductsByRevenue(paidStatuses)
                .stream()
                .map(row -> new TopProductStatsResponse(
                        ((Number) row[0]).longValue(),
                        String.valueOf(row[1]),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).doubleValue()
                ))
                .limit(5)
                .toList();
    }
    @Override
    public List<LowStockProductResponse> getLowStockProducts() {
        int threshold = 5;

        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(row -> new LowStockProductResponse(
                        ((Number) row[0]).longValue(),
                        String.valueOf(row[1]),
                        String.valueOf(row[2]),
                        String.valueOf(row[3]),
                        row[4] != null ? ((Number) row[4]).intValue() : 0
                ))
                .toList();
    }
    @Override
    public List<RevenueByCategoryResponse> getRevenueByCategory() {
        List<OrderStatus> paidStatuses = List.of(
                OrderStatus.PAID,
                OrderStatus.DELIVERED
        );

        return orderItemRepository.findRevenueByCategory(paidStatuses)
                .stream()
                .map(row -> new RevenueByCategoryResponse(
                        ((Number) row[0]).longValue(),
                        String.valueOf(row[1]),
                        ((Number) row[2]).doubleValue()
                ))
                .toList();
    }

}
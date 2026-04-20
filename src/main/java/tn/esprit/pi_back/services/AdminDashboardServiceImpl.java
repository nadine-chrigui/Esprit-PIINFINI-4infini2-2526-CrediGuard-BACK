package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.delivery.DeliveryResponse;
import tn.esprit.pi_back.dto.finance.LowStockProductResponse;
import tn.esprit.pi_back.dto.order.OrderAdminResponse;
import tn.esprit.pi_back.dto.productrequest.ProductRequestResponse;
import tn.esprit.pi_back.entities.Delivery;
import tn.esprit.pi_back.entities.ProductRequest;
import tn.esprit.pi_back.entities.enums.DeliveryStatus;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.entities.enums.ProductRequestStatus;
import tn.esprit.pi_back.mappers.OrderMapper;
import tn.esprit.pi_back.repositories.DeliveryRepository;
import tn.esprit.pi_back.repositories.OrderRepository;
import tn.esprit.pi_back.repositories.ProductRepository;
import tn.esprit.pi_back.repositories.ProductRequestRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int RECENT_LIMIT = 5;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductRequestRepository productRequestRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final EcommerceFinanceStatsService ecommerceFinanceStatsService;
    private final OrderMapper orderMapper;
    private final ProductRequestService productRequestService;
    private final DeliveryService deliveryService;

    @Override
    public Map<String, Object> getDashboard() {
        long openRequests = productRequestRepository.countByStatus(ProductRequestStatus.OPEN);
        long offeredRequests = productRequestRepository.countByStatus(ProductRequestStatus.OFFERED);
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long pendingDeliveries = deliveryRepository.countByDeliveryStatus(DeliveryStatus.PENDING);
        List<LowStockProductResponse> lowStockProducts = ecommerceFinanceStatsService.getLowStockProducts();

        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> overview = new LinkedHashMap<>();
        Map<String, Object> alerts = new LinkedHashMap<>();

        overview.put("totalUsers", userRepository.count());
        overview.put("activeUsers", userRepository.countByEnabledTrue());
        overview.put("disabledUsers", userRepository.countByEnabledFalse());
        overview.put("totalProducts", productRepository.count());
        overview.put("inactiveProducts", productRepository.countByActiveFalse());
        overview.put("openRequests", openRequests);
        overview.put("offeredRequests", offeredRequests);
        overview.put("pendingOrders", pendingOrders);
        overview.put("pendingDeliveries", pendingDeliveries);

        alerts.put("lowStockCount", lowStockProducts.size());
        alerts.put("requestBacklog", openRequests + offeredRequests);
        alerts.put("pendingOrderCount", pendingOrders);
        alerts.put("pendingDeliveryCount", pendingDeliveries);

        response.put("overview", overview);
        response.put("alerts", alerts);
        response.put("financeOverview", ecommerceFinanceStatsService.getOverview());
        response.put("lowStockProducts", lowStockProducts.stream().limit(RECENT_LIMIT).toList());
        response.put("recentOrders", getRecentOrders());
        response.put("recentRequests", getRecentRequests());
        response.put("pendingDeliveries", getPendingDeliveries());

        return response;
    }

    private List<OrderAdminResponse> getRecentOrders() {
        return orderRepository.findAll(PageRequest.of(0, RECENT_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(orderMapper::toAdminResponse)
                .toList();
    }

    private List<ProductRequestResponse> getRecentRequests() {
        return productRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .limit(RECENT_LIMIT)
                .map(ProductRequest::getId)
                .map(productRequestService::getRequestById)
                .toList();
    }

    private List<DeliveryResponse> getPendingDeliveries() {
        return deliveryRepository.findByDeliveryStatusOrderByCreatedAtDesc(DeliveryStatus.PENDING)
                .stream()
                .limit(RECENT_LIMIT)
                .map(Delivery::getId)
                .map(deliveryService::getById)
                .toList();
    }
}

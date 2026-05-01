package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceHistoryResponse;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceMlRequest;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceMlResponse;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceModelInfoResponse;
import tn.esprit.pi_back.dto.productintelligence.ProductIntelligenceResponse;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.ProductIntelligence;
import tn.esprit.pi_back.entities.ProductIntelligenceHistory;
import tn.esprit.pi_back.entities.enums.OrderStatus;
import tn.esprit.pi_back.entities.enums.ProductPerformanceLabel;
import tn.esprit.pi_back.entities.enums.ProductRiskLevel;
import tn.esprit.pi_back.entities.enums.ProductSuggestedAction;
import tn.esprit.pi_back.repositories.OrderItemRepository;
import tn.esprit.pi_back.repositories.ProductIntelligenceHistoryRepository;
import tn.esprit.pi_back.repositories.ProductIntelligenceRepository;
import tn.esprit.pi_back.repositories.ProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductIntelligenceServiceImpl implements ProductIntelligenceService {

    private static final String REASON_SEPARATOR = " | ";

    private final ProductRepository productRepository;
    private final ProductIntelligenceRepository productIntelligenceRepository;
    private final ProductIntelligenceHistoryRepository productIntelligenceHistoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;

    @Value("${product-intelligence.ml.enabled:true}")
    private boolean mlEnabled;

    @Value("${product-intelligence.ml.base-url:http://localhost:5001}")
    private String mlBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public List<ProductIntelligenceResponse> getAll() {
        return productIntelligenceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductIntelligenceResponse getByProductId(Long productId) {
        return productIntelligenceRepository.findByProductId(productId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product intelligence not found for product: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductIntelligenceHistoryResponse> getHistoryByProductId(Long productId) {
        return productIntelligenceHistoryRepository.findTop12ByProductIdOrderByAnalyzedAtDesc(productId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    public ProductIntelligenceResponse analyzeProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        ProductIntelligence intelligence = productIntelligenceRepository.findByProductId(productId)
                .orElseGet(ProductIntelligence::new);

        applyAnalysis(product, intelligence);
        ProductIntelligence saved = productIntelligenceRepository.save(intelligence);
        saveHistorySnapshot(saved);
        return toResponse(saved);
    }

    @Override
    public List<ProductIntelligenceResponse> analyzeAll() {
        return productRepository.findAll()
                .stream()
                .map(product -> {
                    ProductIntelligence intelligence = productIntelligenceRepository.findByProductId(product.getId())
                            .orElseGet(ProductIntelligence::new);
                    applyAnalysis(product, intelligence);
                    ProductIntelligence saved = productIntelligenceRepository.save(intelligence);
                    saveHistorySnapshot(saved);
                    return saved;
                })
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductIntelligenceModelInfoResponse getModelInfo() {
        if (!mlEnabled) {
            return new ProductIntelligenceModelInfoResponse(
                    false,
                    false,
                    "JAVA_RULE_BASED",
                    null,
                    null,
                    "Product intelligence ML integration is disabled."
            );
        }

        try {
            String url = mlBaseUrl.replaceAll("/+$", "") + "/model-info";
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                return fallbackModelInfo("No response from ML service.");
            }

            boolean modelLoaded = Boolean.TRUE.equals(response.get("model_loaded"));
            Object modeObject = response.get("mode");
            String mode = modeObject != null
                    ? String.valueOf(modeObject)
                    : (modelLoaded ? "TRAINED_MODEL" : "RULE_BASED_FALLBACK");

            String modelType = null;
            Integer rows = null;
            Object metadataObject = response.get("metadata");
            if (metadataObject instanceof Map<?, ?> metadata) {
                Object modelTypeObject = metadata.get("model_type");
                Object rowsObject = metadata.get("rows");
                modelType = modelTypeObject != null ? String.valueOf(modelTypeObject) : null;
                rows = rowsObject instanceof Number number ? number.intValue() : null;
            }

            String message = response.get("message") != null
                    ? String.valueOf(response.get("message"))
                    : null;

            return new ProductIntelligenceModelInfoResponse(
                    true,
                    modelLoaded,
                    mode,
                    modelType,
                    rows,
                    message
            );
        } catch (RestClientException ex) {
            return fallbackModelInfo("ML service is not reachable.");
        }
    }

    private ProductIntelligenceModelInfoResponse fallbackModelInfo(String message) {
        return new ProductIntelligenceModelInfoResponse(
                true,
                false,
                "JAVA_RULE_BASED_FALLBACK",
                null,
                null,
                message
        );
    }

    private void applyAnalysis(Product product, ProductIntelligence intelligence) {
        int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int salesLast7Days = getSalesSince(product.getId(), 7);
        int salesLast30Days = getSalesSince(product.getId(), 30);

        if (mlEnabled && applyMlAnalysis(product, intelligence, stock, salesLast7Days, salesLast30Days)) {
            return;
        }

        applyRuleBasedAnalysis(product, intelligence, stock, salesLast7Days, salesLast30Days);
    }

    private boolean applyMlAnalysis(
            Product product,
            ProductIntelligence intelligence,
            int stock,
            int salesLast7Days,
            int salesLast30Days
    ) {
        try {
            String url = mlBaseUrl.replaceAll("/+$", "") + "/predict/product-intelligence";
            ProductIntelligenceMlRequest request = new ProductIntelligenceMlRequest(
                    product.getId(),
                    stock,
                    salesLast7Days,
                    salesLast30Days,
                    product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice()
            );

            ProductIntelligenceMlResponse response = restTemplate.postForObject(
                    url,
                    request,
                    ProductIntelligenceMlResponse.class
            );

            if (response == null) {
                return false;
            }

            intelligence.setProduct(product);
            intelligence.setRiskLevel(ProductRiskLevel.valueOf(response.riskLevel()));
            intelligence.setDaysToStockout(response.daysToStockout() != null ? response.daysToStockout() : 999);
            intelligence.setRecommendedRestock(response.recommendedRestock() != null ? response.recommendedRestock() : 0);
            intelligence.setPerformanceScore(response.performanceScore() != null ? response.performanceScore() : 0);
            intelligence.setPerformanceLabel(ProductPerformanceLabel.valueOf(response.performanceLabel()));
            intelligence.setSuggestedAction(ProductSuggestedAction.valueOf(response.suggestedAction()));
            intelligence.setReasons(String.join(REASON_SEPARATOR, response.reasons() != null ? response.reasons() : List.of()));
            intelligence.setRiskConfidence(response.riskConfidence());
            intelligence.setActionConfidence(response.actionConfidence());
            intelligence.setMlDecision(response.mlDecision());
            intelligence.setMainDrivers(String.join(REASON_SEPARATOR, response.mainDrivers() != null ? response.mainDrivers() : List.of()));
            intelligence.setBusinessRecommendation(response.businessRecommendation());
            intelligence.setModelType(response.modelType());
            intelligence.setSalesLast7Days(salesLast7Days);
            intelligence.setSalesLast30Days(salesLast30Days);
            return true;
        } catch (IllegalArgumentException | RestClientException ex) {
            return false;
        }
    }

    private void applyRuleBasedAnalysis(
            Product product,
            ProductIntelligence intelligence,
            int stock,
            int salesLast7Days,
            int salesLast30Days
    ) {
        double dailyVelocity = salesLast7Days > 0 ? salesLast7Days / 7.0 : salesLast30Days / 30.0;

        int daysToStockout = dailyVelocity > 0
                ? Math.max(1, (int) Math.floor(stock / dailyVelocity))
                : 999;

        ProductRiskLevel riskLevel = resolveRiskLevel(stock, daysToStockout, dailyVelocity);
        ProductPerformanceLabel label = resolvePerformanceLabel(stock, salesLast7Days, salesLast30Days, riskLevel);
        int performanceScore = resolvePerformanceScore(stock, salesLast7Days, salesLast30Days, riskLevel, label);
        int recommendedRestock = resolveRecommendedRestock(stock, dailyVelocity, riskLevel);
        ProductSuggestedAction action = resolveSuggestedAction(riskLevel, label, stock, salesLast30Days);
        List<String> reasons = resolveReasons(stock, salesLast7Days, salesLast30Days, daysToStockout, riskLevel, label);

        intelligence.setProduct(product);
        intelligence.setRiskLevel(riskLevel);
        intelligence.setDaysToStockout(daysToStockout);
        intelligence.setRecommendedRestock(recommendedRestock);
        intelligence.setPerformanceScore(performanceScore);
        intelligence.setPerformanceLabel(label);
        intelligence.setSuggestedAction(action);
        intelligence.setReasons(String.join(REASON_SEPARATOR, reasons));
        intelligence.setRiskConfidence(null);
        intelligence.setActionConfidence(null);
        intelligence.setMlDecision(null);
        intelligence.setMainDrivers(null);
        intelligence.setBusinessRecommendation(null);
        intelligence.setModelType(null);
        intelligence.setSalesLast7Days(salesLast7Days);
        intelligence.setSalesLast30Days(salesLast30Days);
    }

    private int getSalesSince(Long productId, int days) {
        List<OrderStatus> paidStatuses = List.of(OrderStatus.PAID, OrderStatus.DELIVERED);
        Long quantity = orderItemRepository.sumQuantitySoldSince(
                productId,
                paidStatuses,
                LocalDateTime.now().minusDays(days)
        );
        return quantity != null ? quantity.intValue() : 0;
    }

    private ProductRiskLevel resolveRiskLevel(int stock, int daysToStockout, double dailyVelocity) {
        if (stock <= 0 || (dailyVelocity > 0 && daysToStockout <= 5)) {
            return ProductRiskLevel.HIGH;
        }
        if (daysToStockout <= 14) {
            return ProductRiskLevel.MEDIUM;
        }
        return ProductRiskLevel.LOW;
    }

    private ProductPerformanceLabel resolvePerformanceLabel(
            int stock,
            int salesLast7Days,
            int salesLast30Days,
            ProductRiskLevel riskLevel
    ) {
        if (riskLevel == ProductRiskLevel.HIGH && salesLast7Days > 0) {
            return ProductPerformanceLabel.AT_RISK;
        }
        if (salesLast30Days >= 50 || salesLast7Days >= 15) {
            return ProductPerformanceLabel.BEST_SELLER;
        }
        if (salesLast30Days <= 3 && stock >= 20) {
            return ProductPerformanceLabel.SLOW_MOVING;
        }
        return ProductPerformanceLabel.STABLE;
    }

    private int resolvePerformanceScore(
            int stock,
            int salesLast7Days,
            int salesLast30Days,
            ProductRiskLevel riskLevel,
            ProductPerformanceLabel label
    ) {
        int score = 45;
        score += Math.min(30, salesLast30Days);
        score += Math.min(15, salesLast7Days);

        if (label == ProductPerformanceLabel.BEST_SELLER) {
            score += 15;
        }
        if (label == ProductPerformanceLabel.SLOW_MOVING) {
            score -= 20;
        }
        if (riskLevel == ProductRiskLevel.HIGH) {
            score -= 15;
        }
        if (stock <= 0) {
            score -= 10;
        }

        return Math.max(0, Math.min(100, score));
    }

    private int resolveRecommendedRestock(int stock, double dailyVelocity, ProductRiskLevel riskLevel) {
        if (riskLevel == ProductRiskLevel.LOW || dailyVelocity <= 0) {
            return 0;
        }

        int targetStockFor30Days = (int) Math.ceil(dailyVelocity * 30);
        return Math.max(0, targetStockFor30Days - stock);
    }

    private ProductSuggestedAction resolveSuggestedAction(
            ProductRiskLevel riskLevel,
            ProductPerformanceLabel label,
            int stock,
            int salesLast30Days
    ) {
        if (riskLevel == ProductRiskLevel.HIGH || stock <= 0) {
            return ProductSuggestedAction.RESTOCK;
        }
        if (label == ProductPerformanceLabel.SLOW_MOVING || salesLast30Days <= 3) {
            return ProductSuggestedAction.PROMOTE;
        }
        if (riskLevel == ProductRiskLevel.MEDIUM) {
            return ProductSuggestedAction.MONITOR;
        }
        return ProductSuggestedAction.KEEP;
    }

    private List<String> resolveReasons(
            int stock,
            int salesLast7Days,
            int salesLast30Days,
            int daysToStockout,
            ProductRiskLevel riskLevel,
            ProductPerformanceLabel label
    ) {
        List<String> reasons = new ArrayList<>();

        if (stock <= 0) {
            reasons.add("Product is currently out of stock.");
        }
        if (riskLevel == ProductRiskLevel.HIGH && stock > 0) {
            reasons.add("Stock may run out in " + daysToStockout + " day(s).");
        }
        if (salesLast7Days > 0) {
            reasons.add(salesLast7Days + " unit(s) sold in the last 7 days.");
        }
        if (salesLast30Days > 0) {
            reasons.add(salesLast30Days + " unit(s) sold in the last 30 days.");
        }
        if (label == ProductPerformanceLabel.SLOW_MOVING) {
            reasons.add("Low sales velocity compared with available stock.");
        }
        if (reasons.isEmpty()) {
            reasons.add("No recent risk detected from current sales and stock data.");
        }

        return reasons;
    }

    private void saveHistorySnapshot(ProductIntelligence intelligence) {
        Product product = intelligence.getProduct();
        ProductIntelligenceHistory history = new ProductIntelligenceHistory();

        history.setProduct(product);
        history.setRiskLevel(intelligence.getRiskLevel());
        history.setDaysToStockout(intelligence.getDaysToStockout());
        history.setRecommendedRestock(intelligence.getRecommendedRestock());
        history.setPerformanceScore(intelligence.getPerformanceScore());
        history.setPerformanceLabel(intelligence.getPerformanceLabel());
        history.setSuggestedAction(intelligence.getSuggestedAction());
        history.setReasons(intelligence.getReasons());
        history.setRiskConfidence(intelligence.getRiskConfidence());
        history.setActionConfidence(intelligence.getActionConfidence());
        history.setMlDecision(intelligence.getMlDecision());
        history.setMainDrivers(intelligence.getMainDrivers());
        history.setBusinessRecommendation(intelligence.getBusinessRecommendation());
        history.setModelType(intelligence.getModelType());
        history.setCurrentStock(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
        history.setSalesLast7Days(intelligence.getSalesLast7Days());
        history.setSalesLast30Days(intelligence.getSalesLast30Days());
        history.setAnalyzedAt(LocalDateTime.now());

        productIntelligenceHistoryRepository.save(history);
    }

    private ProductIntelligenceResponse toResponse(ProductIntelligence intelligence) {
        Product product = intelligence.getProduct();
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        List<String> reasons = intelligence.getReasons() == null || intelligence.getReasons().isBlank()
                ? List.of()
                : Arrays.stream(intelligence.getReasons().split("\\Q" + REASON_SEPARATOR + "\\E"))
                .filter(reason -> !reason.isBlank())
                .toList();
        List<String> mainDrivers = intelligence.getMainDrivers() == null || intelligence.getMainDrivers().isBlank()
                ? List.of()
                : Arrays.stream(intelligence.getMainDrivers().split("\\Q" + REASON_SEPARATOR + "\\E"))
                .filter(driver -> !driver.isBlank())
                .toList();

        return new ProductIntelligenceResponse(
                intelligence.getId(),
                product.getId(),
                product.getName(),
                categoryName,
                product.getStockQuantity() != null ? product.getStockQuantity() : 0,
                intelligence.getSalesLast7Days(),
                intelligence.getSalesLast30Days(),
                intelligence.getRiskLevel(),
                intelligence.getDaysToStockout(),
                intelligence.getRecommendedRestock(),
                intelligence.getPerformanceScore(),
                intelligence.getPerformanceLabel(),
                intelligence.getSuggestedAction(),
                reasons,
                intelligence.getRiskConfidence(),
                intelligence.getActionConfidence(),
                intelligence.getMlDecision(),
                mainDrivers,
                intelligence.getBusinessRecommendation(),
                intelligence.getModelType(),
                intelligence.getAnalyzedAt()
        );
    }

    private ProductIntelligenceHistoryResponse toHistoryResponse(ProductIntelligenceHistory history) {
        Product product = history.getProduct();
        List<String> reasons = splitList(history.getReasons());
        List<String> mainDrivers = splitList(history.getMainDrivers());

        return new ProductIntelligenceHistoryResponse(
                history.getId(),
                product.getId(),
                product.getName(),
                history.getCurrentStock(),
                history.getSalesLast7Days(),
                history.getSalesLast30Days(),
                history.getRiskLevel(),
                history.getDaysToStockout(),
                history.getRecommendedRestock(),
                history.getPerformanceScore(),
                history.getPerformanceLabel(),
                history.getSuggestedAction(),
                reasons,
                history.getRiskConfidence(),
                history.getActionConfidence(),
                history.getMlDecision(),
                mainDrivers,
                history.getBusinessRecommendation(),
                history.getModelType(),
                history.getAnalyzedAt()
        );
    }

    private List<String> splitList(String value) {
        return value == null || value.isBlank()
                ? List.of()
                : Arrays.stream(value.split("\\Q" + REASON_SEPARATOR + "\\E"))
                .filter(item -> !item.isBlank())
                .toList();
    }
}

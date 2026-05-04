package tn.esprit.pi_back.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.pi_back.dto.Ml.CrowdfundingSuccessPredictionResponse;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.InvestorClassification;
import tn.esprit.pi_back.repositories.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CrowdfundingSuccessPredictionServiceImpl implements CrowdfundingSuccessPredictionService {

    private final CrowdfundingProjectRepository projectRepository;
    private final ProjectSuccessPredictionRepository predictionRepository;
    private final InvestmentRepository investmentRepository;
    private final PurchaseOptionRepository purchaseOptionRepository;
    private final OptionSubscriptionRepository optionSubscriptionRepository;
    private final BlogPostRepository blogPostRepository;
    private final InvestorAnalyticsSnapshotRepository investorAnalyticsSnapshotRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ml.flask.base-url:http://127.0.0.1:5001}")
    private String flaskBaseUrl;

    @Override
    public CrowdfundingSuccessPredictionResponse refreshPrediction(Long projectId) {
        CrowdfundingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        PredictionFeatures features = buildFeatures(project);
        JsonNode flaskResponse = invokePredictionApi(features);

        ProjectSuccessPrediction prediction = new ProjectSuccessPrediction();
        prediction.setProject(project);
        prediction.setSuccessProbability(flaskResponse.path("successProbability").asDouble());
        prediction.setPredictedLabel(flaskResponse.path("predictedLabel").asText("UNKNOWN"));
        prediction.setModelVersion(flaskResponse.path("modelVersion").asText("unknown"));
        prediction.setTrainingAccuracy(flaskResponse.path("trainingAccuracy").asDouble(0.0));
        prediction.setValidationAccuracy(flaskResponse.path("validationAccuracy").asDouble(0.0));
        prediction.setDaysSinceLaunch(features.daysSinceLaunch());
        prediction.setCampaignDurationDays(features.campaignDurationDays());
        prediction.setFundingProgressRatio(features.fundingProgressRatio());
        prediction.setCollectedAmount(features.collectedAmount());
        prediction.setFundingGoal(features.fundingGoal());
        prediction.setAverageInvestmentAmount(features.averageInvestmentAmount());
        prediction.setInvestmentCount(features.investmentCount());
        prediction.setPurchaseOptionSubscriptionsCount(features.purchaseOptionSubscriptionsCount());
        prediction.setBlogPostsLast7Days(features.blogPostsLast7Days());
        prediction.setPremiumInvestorRatio(features.premiumInvestorRatio());
        prediction.setStableInvestorRatio(features.stableInvestorRatio());
        prediction.setRiskyInvestorRatio(features.riskyInvestorRatio());
        prediction.setGeneratedAt(LocalDateTime.now());

        return toResponse(predictionRepository.save(prediction));
    }

    @Override
    @Transactional(readOnly = true)
    public CrowdfundingSuccessPredictionResponse getLatestPrediction(Long projectId) {
        return predictionRepository.findTopByProjectProjectIdOrderByGeneratedAtDesc(projectId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No ML prediction found for this project yet"
                ));
    }

    private PredictionFeatures buildFeatures(CrowdfundingProject project) {
        List<Investment> investments = investmentRepository.findByProjectProjectId(project.getProjectId())
                .stream()
                .filter(investment -> investment.getStatus() == Investment.InvestmentStatus.ACTIVE
                        || investment.getStatus() == Investment.InvestmentStatus.COMPLETED)
                .toList();

        List<PurchaseOption> purchaseOptions = purchaseOptionRepository.findByProjectProjectId(project.getProjectId());
        List<BlogPost> blogPosts = blogPostRepository.findByProjectProjectId(project.getProjectId());
        LocalDate today = LocalDate.now();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        int optionSubscriptionsCount = purchaseOptions.stream()
                .mapToInt(option -> optionSubscriptionRepository.findByPurchaseOptionOptionId(option.getOptionId()).stream()
                        .filter(subscription -> subscription.getStatus() != OptionSubscription.SubscriptionStatus.CANCELLED)
                        .mapToInt(OptionSubscription::getReservedQuantity)
                        .sum())
                .sum();

        int recentPublishedPosts = (int) blogPosts.stream()
                .filter(post -> post.getStatus() == BlogPost.PostStatus.PUBLISHED)
                .filter(post -> post.getCreatedAt() != null && !post.getCreatedAt().isBefore(sevenDaysAgo))
                .count();

        double totalInvestmentAmount = investments.stream()
                .mapToDouble(Investment::getAmount)
                .sum();
        int investmentCount = investments.size();
        double averageInvestment = investmentCount > 0 ? round(totalInvestmentAmount / investmentCount) : 0.0;

        List<InvestorAnalyticsSnapshot> snapshots = investments.stream()
                .map(investment -> investorAnalyticsSnapshotRepository.findTopByInvestmentInvestmentIdOrderByCreatedAtDesc(investment.getInvestmentId()).orElse(null))
                .filter(snapshot -> snapshot != null)
                .toList();

        double premiumRatio = ratioForClassification(snapshots, InvestorClassification.PREMIUM);
        double riskyRatio = ratioForClassification(snapshots, InvestorClassification.RISKY);
        double stableRatio = snapshots.isEmpty()
                ? 0.0
                : Math.max(0.0, 1.0 - premiumRatio - riskyRatio);

        int daysSinceLaunch = (int) Math.max(0, ChronoUnit.DAYS.between(project.getStartDate(), today));
        int campaignDurationDays = (int) Math.max(1, ChronoUnit.DAYS.between(project.getStartDate(), project.getEndDate()) + 1);
        double fundingProgressRatio = project.getFundingGoal() > 0
                ? round(project.getCollectedAmount() / project.getFundingGoal())
                : 0.0;

        return new PredictionFeatures(
                daysSinceLaunch,
                campaignDurationDays,
                fundingProgressRatio,
                round(project.getCollectedAmount()),
                round(project.getFundingGoal()),
                averageInvestment,
                investmentCount,
                optionSubscriptionsCount,
                recentPublishedPosts,
                round(premiumRatio),
                round(stableRatio),
                round(riskyRatio)
        );
    }

    private JsonNode invokePredictionApi(PredictionFeatures features) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("days_since_launch", features.daysSinceLaunch());
        payload.put("campaign_duration_days", features.campaignDurationDays());
        payload.put("funding_progress_ratio", features.fundingProgressRatio());
        payload.put("collected_amount", features.collectedAmount());
        payload.put("funding_goal", features.fundingGoal());
        payload.put("average_investment_amount", features.averageInvestmentAmount());
        payload.put("investment_count", features.investmentCount());
        payload.put("purchase_option_subscriptions_count", features.purchaseOptionSubscriptionsCount());
        payload.put("blog_posts_last_7_days", features.blogPostsLast7Days());
        payload.put("premium_investor_ratio", features.premiumInvestorRatio());
        payload.put("stable_investor_ratio", features.stableInvestorRatio());
        payload.put("risky_investor_ratio", features.riskyInvestorRatio());

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(flaskBaseUrl + "/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Flask ML service returned " + response.statusCode() + ": " + response.body()
                );
            }
            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to reach Flask ML service",
                    ex
            );
        }
    }

    private double ratioForClassification(List<InvestorAnalyticsSnapshot> snapshots, InvestorClassification classification) {
        if (snapshots.isEmpty()) {
            return 0.0;
        }

        long matchingCount = snapshots.stream()
                .filter(snapshot -> classification == snapshot.getClassification()
                        || (classification == InvestorClassification.STABLE
                        && snapshot.getClassification() == InvestorClassification.PROFITABLE))
                .count();

        return (double) matchingCount / snapshots.size();
    }

    private CrowdfundingSuccessPredictionResponse toResponse(ProjectSuccessPrediction prediction) {
        return new CrowdfundingSuccessPredictionResponse(
                prediction.getId(),
                prediction.getProject().getProjectId(),
                prediction.getSuccessProbability(),
                prediction.getPredictedLabel(),
                prediction.getModelVersion(),
                prediction.getTrainingAccuracy(),
                prediction.getValidationAccuracy(),
                prediction.getDaysSinceLaunch(),
                prediction.getCampaignDurationDays(),
                prediction.getFundingProgressRatio(),
                prediction.getCollectedAmount(),
                prediction.getFundingGoal(),
                prediction.getAverageInvestmentAmount(),
                prediction.getInvestmentCount(),
                prediction.getPurchaseOptionSubscriptionsCount(),
                prediction.getBlogPostsLast7Days(),
                prediction.getPremiumInvestorRatio(),
                prediction.getStableInvestorRatio(),
                prediction.getRiskyInvestorRatio(),
                prediction.getGeneratedAt()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record PredictionFeatures(
            int daysSinceLaunch,
            int campaignDurationDays,
            double fundingProgressRatio,
            double collectedAmount,
            double fundingGoal,
            double averageInvestmentAmount,
            int investmentCount,
            int purchaseOptionSubscriptionsCount,
            int blogPostsLast7Days,
            double premiumInvestorRatio,
            double stableInvestorRatio,
            double riskyInvestorRatio
    ) {
    }
}

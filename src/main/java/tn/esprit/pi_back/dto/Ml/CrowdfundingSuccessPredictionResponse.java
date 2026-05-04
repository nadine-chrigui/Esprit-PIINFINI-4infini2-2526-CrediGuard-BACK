package tn.esprit.pi_back.dto.Ml;

import java.time.LocalDateTime;

public record CrowdfundingSuccessPredictionResponse(
        Long id,
        Long projectId,
        Double successProbability,
        String predictedLabel,
        String modelVersion,
        Double trainingAccuracy,
        Double validationAccuracy,
        Integer daysSinceLaunch,
        Integer campaignDurationDays,
        Double fundingProgressRatio,
        Double collectedAmount,
        Double fundingGoal,
        Double averageInvestmentAmount,
        Integer investmentCount,
        Integer purchaseOptionSubscriptionsCount,
        Integer blogPostsLast7Days,
        Double premiumInvestorRatio,
        Double stableInvestorRatio,
        Double riskyInvestorRatio,
        LocalDateTime generatedAt
) {}

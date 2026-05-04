package tn.esprit.pi_back.dto.Crowdfunding;

import java.time.LocalDate;

public record CrowdfundingResponse(

        Long id,
        String title,
        String description,
        Double fundingGoal,
        Double collectedAmount,
        Double interestRate,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Long ownerId

) {}
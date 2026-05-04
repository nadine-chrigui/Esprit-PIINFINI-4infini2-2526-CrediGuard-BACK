package tn.esprit.pi_back.dto.Crowdfunding;

import java.time.LocalDate;

public record CrowdfundingUpdateRequest(

        String title,
        String description,
        Double fundingGoal,
        Double interestRate,
        LocalDate startDate,
        LocalDate endDate

) {}
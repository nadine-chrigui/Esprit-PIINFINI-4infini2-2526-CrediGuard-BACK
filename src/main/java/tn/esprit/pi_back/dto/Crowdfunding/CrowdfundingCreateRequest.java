package tn.esprit.pi_back.dto.Crowdfunding;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CrowdfundingCreateRequest(

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 50)
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 2000)
        String description,

        @NotNull @Positive
        Double fundingGoal,

        @NotNull
        Double interestRate,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        Long ownerId

) {}
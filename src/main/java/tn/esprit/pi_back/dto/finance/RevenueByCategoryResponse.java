package tn.esprit.pi_back.dto.finance;

public record RevenueByCategoryResponse(
        Long categoryId,
        String categoryName,
        double revenue
) {
}
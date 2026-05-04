package tn.esprit.pi_back.dto.productintelligence;

public record ProductIntelligenceModelInfoResponse(
        boolean mlEnabled,
        boolean modelLoaded,
        String mode,
        String modelType,
        Integer rows,
        String message
) {
}

package tn.esprit.pi_back.dto.insurance;

import java.util.Map;

public record SegmentationResponseDTO(
        String segment,
        double confidence,
        String interpretation,
        Map<String, Double> probabilities
) {}

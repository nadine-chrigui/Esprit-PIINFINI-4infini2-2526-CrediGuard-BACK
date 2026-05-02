package tn.esprit.pi_back.dto.insurance;

import java.time.LocalDateTime;

public record RiskScoreDTO(
        Long id,
        Integer globalScore,
        Integer sectorScore,
        Integer regionScore,
        Integer historyScore,
        String factorDetails,
        LocalDateTime computedAt
) {}

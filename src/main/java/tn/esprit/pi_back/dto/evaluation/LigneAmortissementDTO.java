package tn.esprit.pi_back.dto.evaluation;

import java.time.LocalDate;

public record LigneAmortissementDTO(
        Integer numeroEcheance,
        LocalDate dateEcheance,
        String phase,
        Double mensualite,
        Double interet,
        Double capitalRembourse,
        Double capitalRestantDu
) {}

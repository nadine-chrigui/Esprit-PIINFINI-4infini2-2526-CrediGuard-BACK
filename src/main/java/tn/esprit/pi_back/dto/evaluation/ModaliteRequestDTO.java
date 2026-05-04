package tn.esprit.pi_back.dto.evaluation;

import jakarta.validation.constraints.NotNull;
import tn.esprit.pi_back.entities.enums.TypeModalite;

public record ModaliteRequestDTO(

        @NotNull
        TypeModalite modaliteChoisie,

        String commentaireAdmin,

        String choisiePar
) {}

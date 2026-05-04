package tn.esprit.pi_back.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreditRiskFlaskResponseDTO(

        @JsonProperty("probability_default")
        Double probabilityDefault,

        @JsonProperty("decision_base")
        String decisionBase,

        @JsonProperty("credit_score")
        Double creditScore,

        @JsonProperty("risk_class_base")
        String riskClassBase,

        @JsonProperty("score_band_base")
        String scoreBandBase,

        @JsonProperty("mc_mean_proba")
        Double mcMeanProba,

        @JsonProperty("mc_std")
        Double mcStd,

        @JsonProperty("var_95")
        Double var95,

        @JsonProperty("var_99")
        Double var99,

        @JsonProperty("cvar_95")
        Double cvar95,

        @JsonProperty("ci_95_lower")
        Double ci95Lower,

        @JsonProperty("ci_95_upper")
        Double ci95Upper,

        @JsonProperty("n_simulations")
        Integer nSimulations,

        @JsonProperty("decision_conservative")
        String decisionConservative,

        @JsonProperty("risk_class_conservative")
        String riskClassConservative,

        @JsonProperty("score_band_conservative")
        String scoreBandConservative,

        @JsonProperty("credit_score_mc")
        Double creditScoreMc,

        @JsonProperty("high_uncertainty")
        Boolean highUncertainty,

        @JsonProperty("model")
        String model
) {
}

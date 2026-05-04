package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pi_back.constants.MLFeatures;
import tn.esprit.pi_back.dto.evaluation.CreditRiskFlaskResponseDTO;
import tn.esprit.pi_back.dto.evaluation.EvaluationPredictionRequestDTO;
import tn.esprit.pi_back.dto.evaluation.EvaluationRisqueRequestDTO;
import tn.esprit.pi_back.dto.evaluation.EvaluationRisqueResponseDTO;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.EvaluationRisque;
import tn.esprit.pi_back.entities.ProfilCredit;
import tn.esprit.pi_back.entities.enums.DecisionSuggeree;
import tn.esprit.pi_back.entities.enums.NiveauRisque;
import tn.esprit.pi_back.entities.enums.ProfilLoanGrade;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;
import tn.esprit.pi_back.repositories.EvaluationRisqueRepository;
import tn.esprit.pi_back.repositories.ProfilCreditRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationRisqueServiceImpl implements EvaluationRisqueService {

    private final EvaluationRisqueRepository evaluationRepo;
    private final DemandeCreditRepository demandeRepo;
    private final ProfilCreditRepository profilRepo;
    private final RestTemplate restTemplate;

    @Value("${credit-risk.flask.base-url:http://localhost:5000}")
    private String flaskBaseUrl;

    @Override
    public EvaluationRisqueResponseDTO create(Long demandeId, EvaluationRisqueRequestDTO dto) {

        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (evaluationRepo.existsByDemandeCreditId(demandeId)) {
            throw new IllegalStateException("Evaluation already exists for this demande");
        }

        if (demande.getStatut() != StatutDemande.EN_COURS_D_ETUDE) {
            throw new IllegalStateException("Evaluation possible only if demande is EN_COURS_D_ETUDE");
        }

        EvaluationRisque evaluation = new EvaluationRisque();
        evaluation.setScore(dto.score());
        evaluation.setProbabiliteDefaut(dto.probabiliteDefaut());
        evaluation.setVersionModele(dto.versionModele());
        evaluation.setDemandeCredit(demande);
        evaluation.setDateEvaluation(LocalDateTime.now());

        if (dto.probabiliteDefaut() < 0.2) {
            evaluation.setNiveauRisque(NiveauRisque.FAIBLE);
            evaluation.setDecisionSuggeree(DecisionSuggeree.ACCEPTER);
        } else if (dto.probabiliteDefaut() < 0.5) {
            evaluation.setNiveauRisque(NiveauRisque.MOYEN);
            evaluation.setDecisionSuggeree(DecisionSuggeree.CONDITIONS);
        } else {
            evaluation.setNiveauRisque(NiveauRisque.ELEVE);
            evaluation.setDecisionSuggeree(DecisionSuggeree.REFUSER);
        }

        EvaluationRisque saved = evaluationRepo.save(evaluation);
        return toDTO(saved);
    }

    @Override
    public EvaluationRisqueResponseDTO predictWithModel(Long demandeId, EvaluationPredictionRequestDTO dto) {
        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (demande.getStatut() != StatutDemande.EN_COURS_D_ETUDE) {
            throw new IllegalStateException("Prediction is allowed only if demande is EN_COURS_D_ETUDE");
        }

        ProfilCredit profil = profilRepo.findByClientId(demande.getClient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfilCredit not found for client: " + demande.getClient().getId()));

        ProfilLoanGrade effectiveLoanGrade = resolveLoanGrade(dto, profil);
        Double effectiveInterestRate = resolveInterestRate(dto, profil);

        profil.setProposedLoanGrade(effectiveLoanGrade);
        profil.setProposedInterestRate(effectiveInterestRate);
        profilRepo.save(profil);

        Map<String, Object> payload = buildFlaskPayload(demande, profil, effectiveLoanGrade, effectiveInterestRate, dto);

        CreditRiskFlaskResponseDTO flaskResponse;
        try {
            flaskResponse = restTemplate.postForObject(
                    flaskBaseUrl + "/predict/mc",
                    payload,
                    CreditRiskFlaskResponseDTO.class
            );
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to call Flask prediction service: " + ex.getMessage());
        }

        if (flaskResponse == null) {
            throw new IllegalStateException("Empty response from Flask prediction service");
        }

        EvaluationRisque evaluation = evaluationRepo.findByDemandeCreditId(demandeId)
                .orElseGet(EvaluationRisque::new);

        evaluation.setDemandeCredit(demande);
        evaluation.setDateEvaluation(LocalDateTime.now());

        applyFlaskResponse(evaluation, flaskResponse);

        EvaluationRisque saved = evaluationRepo.save(evaluation);
        return toDTO(saved);
    }

    @Override
    public EvaluationRisqueResponseDTO getByDemande(Long demandeId) {
        EvaluationRisque evaluation = evaluationRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found"));

        return toDTO(evaluation);
    }

    private Map<String, Object> buildFlaskPayload(
            DemandeCredit demande,
            ProfilCredit profil,
            ProfilLoanGrade loanGrade,
            Double interestRate,
            EvaluationPredictionRequestDTO dto
    ) {
        Map<String, Object> payload = new HashMap<>();

        payload.put(MLFeatures.PERSON_AGE, profil.getPersonAge());
        payload.put(MLFeatures.PERSON_INCOME, profil.getPersonIncomeAnnual());
        payload.put(MLFeatures.PERSON_HOME_OWNERSHIP, profil.getPersonHomeOwnership().name());
        payload.put(MLFeatures.PERSON_EMP_LENGTH, profil.getPersonEmploymentLength());
        payload.put(MLFeatures.CB_PERSON_DEFAULT_ON_FILE, profil.getPreviousDefaultOnFile().name());
        payload.put(MLFeatures.CB_PERSON_CRED_HIST_LENGTH, profil.getCreditHistoryLength());
        payload.put(MLFeatures.LOAN_INTENT, profil.getLoanIntent().name());
        payload.put(MLFeatures.LOAN_GRADE, loanGrade.name());
        payload.put(MLFeatures.LOAN_AMNT, demande.getMontantDemande());
        payload.put(MLFeatures.LOAN_INT_RATE, interestRate);
        payload.put(MLFeatures.LOAN_PERCENT_INCOME, demande.getMontantDemande() / profil.getPersonIncomeAnnual());

        if (dto != null && dto.nSim() != null) {
            payload.put(MLFeatures.N_SIM, dto.nSim());
        }

        if (dto != null && dto.noiseFactor() != null) {
            payload.put(MLFeatures.NOISE_FACTOR, dto.noiseFactor());
        }

        // ── Step 6: Verify payload (Very Important) ───────────────────────────────
        System.out.println("ML Risk Prediction Payload: " + payload);

        return payload;
    }

    private ProfilLoanGrade resolveLoanGrade(EvaluationPredictionRequestDTO dto, ProfilCredit profil) {
        if (dto != null && dto.loanGrade() != null) {
            return dto.loanGrade();
        }
        if (profil.getProposedLoanGrade() != null) {
            return profil.getProposedLoanGrade();
        }
        throw new IllegalStateException("Admin must provide loan grade before prediction");
    }

    private Double resolveInterestRate(EvaluationPredictionRequestDTO dto, ProfilCredit profil) {
        if (dto != null && dto.interestRate() != null) {
            return dto.interestRate();
        }
        if (profil.getProposedInterestRate() != null) {
            return profil.getProposedInterestRate();
        }
        throw new IllegalStateException("Admin must provide interest rate before prediction");
    }

    private void applyFlaskResponse(EvaluationRisque evaluation, CreditRiskFlaskResponseDTO response) {
        Double baseProba = defaultDouble(response.probabilityDefault(), 0.0);
        Double conservativeProba = defaultDouble(response.var95(), baseProba);

        Double baseScore = defaultDouble(response.creditScore(), (1 - baseProba) * 100);
        Double conservativeScore = defaultDouble(response.creditScoreMc(), (1 - conservativeProba) * 100);

        evaluation.setScore(conservativeScore);
        evaluation.setProbabiliteDefaut(conservativeProba);
        evaluation.setVersionModele(defaultString(response.model(), "XGBoost + Monte Carlo"));
        evaluation.setNiveauRisque(mapNiveau(conservativeProba));
        evaluation.setDecisionSuggeree(mapDecision(conservativeProba));

        evaluation.setScoreBase(baseScore);
        evaluation.setScoreConservateur(conservativeScore);
        evaluation.setProbabiliteDefautBase(baseProba);
        evaluation.setProbabiliteDefautConservative(conservativeProba);
        evaluation.setMcStd(response.mcStd());
        evaluation.setVar95(response.var95());
        evaluation.setVar99(response.var99());
        evaluation.setCvar95(response.cvar95());
        evaluation.setCi95Lower(response.ci95Lower());
        evaluation.setCi95Upper(response.ci95Upper());
        evaluation.setHighUncertainty(response.highUncertainty());
        evaluation.setRiskClassBase(response.riskClassBase());
        evaluation.setRiskClassConservative(response.riskClassConservative());
        evaluation.setScoreBandBase(response.scoreBandBase());
        evaluation.setScoreBandConservative(response.scoreBandConservative());
        evaluation.setDecisionBase(response.decisionBase());
        evaluation.setDecisionConservative(response.decisionConservative());
    }

    private NiveauRisque mapNiveau(Double proba) {
        if (proba < 0.2) {
            return NiveauRisque.FAIBLE;
        }
        if (proba < 0.5) {
            return NiveauRisque.MOYEN;
        }
        return NiveauRisque.ELEVE;
    }

    private DecisionSuggeree mapDecision(Double proba) {
        if (proba < 0.2) {
            return DecisionSuggeree.ACCEPTER;
        }
        if (proba < 0.5) {
            return DecisionSuggeree.CONDITIONS;
        }
        return DecisionSuggeree.REFUSER;
    }

    private Double defaultDouble(Double value, Double fallback) {
        return value != null ? value : fallback;
    }

    private String defaultString(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    private EvaluationRisqueResponseDTO toDTO(EvaluationRisque e) {
        return new EvaluationRisqueResponseDTO(
                e.getId(),
                e.getScore(),
                e.getNiveauRisque(),
                e.getProbabiliteDefaut(),
                e.getVersionModele(),
                e.getDecisionSuggeree(),
                e.getDateEvaluation(),
                e.getDemandeCredit().getId(),
                e.getScoreBase(),
                e.getScoreConservateur(),
                e.getProbabiliteDefautBase(),
                e.getProbabiliteDefautConservative(),
                e.getMcStd(),
                e.getVar95(),
                e.getVar99(),
                e.getCvar95(),
                e.getCi95Lower(),
                e.getCi95Upper(),
                e.getHighUncertainty(),
                e.getRiskClassBase(),
                e.getRiskClassConservative(),
                e.getScoreBandBase(),
                e.getScoreBandConservative(),
                e.getDecisionBase(),
                e.getDecisionConservative()
        );
    }
}

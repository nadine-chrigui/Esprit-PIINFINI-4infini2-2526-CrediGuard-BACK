package tn.esprit.pi_back.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.EvaluationRisque;
import tn.esprit.pi_back.entities.Modalite;
import tn.esprit.pi_back.entities.PlanUtilisationCredit;
import tn.esprit.pi_back.entities.ProfilCredit;
import tn.esprit.pi_back.entities.enums.ProfilLoanGrade;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;
import tn.esprit.pi_back.repositories.EvaluationRisqueRepository;
import tn.esprit.pi_back.repositories.ModaliteRepository;
import tn.esprit.pi_back.repositories.PlanUtilisationRepository;
import tn.esprit.pi_back.repositories.ProfilCreditRepository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialAdvisorServiceImpl implements FinancialAdvisorService {

    private final DemandeCreditRepository demandeRepo;
    private final ProfilCreditRepository profilRepo;
    private final EvaluationRisqueRepository evaluationRepo;
    private final PlanUtilisationRepository planRepo;
    private final ModaliteRepository modaliteRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${credit-risk.flask.base-url:http://localhost:5000}")
    private String flaskBaseUrl;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:crediguard-finance}")
    private String ollamaModel;

    @Override
    public Map<String, Object> getAdvisorReport(Long demandeId) {
        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        ProfilCredit profil = profilRepo.findByClientId(demande.getClient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Credit profile not found"));

        EvaluationRisque evaluation = evaluationRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Risk evaluation not found. Run prediction first."));

        PlanUtilisationCredit plan = planRepo.findByDemandeCreditId(demandeId).orElse(null);
        Modalite modalite = modaliteRepo.findByDemandeCreditId(demandeId).orElse(null);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("n_sim", 500);
        request.put("noise_factor", 0.01);
        request.put("profile", buildProfilePayload(demande, profil));
        request.put("context", buildContextPayload(demande, profil, evaluation, plan, modalite));

        Map<String, Object> advisorResponse = callFlaskAdvisor(request);
        addOllamaNarrative(advisorResponse, request);

        return advisorResponse;
    }

    private Map<String, Object> buildProfilePayload(DemandeCredit demande, ProfilCredit profil) {
        ProfilLoanGrade loanGrade = profil.getProposedLoanGrade();
        Double interestRate = profil.getProposedInterestRate();

        if (loanGrade == null || interestRate == null) {
            throw new IllegalStateException("Run ML prediction first. Loan grade and interest rate are missing.");
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("person_age", profil.getPersonAge());
        profile.put("person_income", profil.getPersonIncomeAnnual());
        profile.put("person_home_ownership", profil.getPersonHomeOwnership().name());
        profile.put("person_emp_length", profil.getPersonEmploymentLength());
        profile.put("cb_person_default_on_file", profil.getPreviousDefaultOnFile().name());
        profile.put("cb_person_cred_hist_length", profil.getCreditHistoryLength());
        profile.put("loan_intent", profil.getLoanIntent().name());
        profile.put("loan_grade", loanGrade.name());
        profile.put("loan_amnt", demande.getMontantDemande());
        profile.put("loan_int_rate", interestRate);
        profile.put("loan_percent_income", demande.getMontantDemande() / profil.getPersonIncomeAnnual());
        return profile;
    }

    private Map<String, Object> buildContextPayload(
            DemandeCredit demande,
            ProfilCredit profil,
            EvaluationRisque evaluation,
            PlanUtilisationCredit plan,
            Modalite modalite
    ) {
        Map<String, Object> context = new LinkedHashMap<>();

        context.put("demandeId", demande.getId());
        context.put("reference", demande.getReference());
        context.put("clientName", demande.getClient().getFullName());
        context.put("typeCredit", demande.getTypeCredit().name());
        context.put("requestedAmount", demande.getMontantDemande());
        context.put("durationMonths", demande.getDureeMois());
        context.put("creditPurpose", demande.getObjetCredit());

        context.put("hasExistingClients", profil.getHasExistingClients());
        context.put("projectStartDelayMonths", profil.getProjectStartDelayMonths());
        context.put("expectedMonthlyRevenueAfterStart", profil.getExpectedMonthlyRevenueAfterStart());
        context.put("monthlyFixedCharges", profil.getMonthlyFixedCharges());
        context.put("existingLoanMonthlyPayments", profil.getExistingLoanMonthlyPayments());
        context.put("outstandingOldDebt", profil.getOutstandingOldDebt());
        context.put("needsGracePeriod", profil.getNeedsGracePeriod());

        context.put("score", evaluation.getScore());
        context.put("defaultProbability", evaluation.getProbabiliteDefaut());
        context.put("var95", evaluation.getVar95());
        context.put("var99", evaluation.getVar99());
        context.put("cvar95", evaluation.getCvar95());
        context.put("uncertainty", evaluation.getMcStd());
        context.put("highUncertainty", evaluation.getHighUncertainty());
        context.put("suggestedDecision", evaluation.getDecisionSuggeree().name());

        if (plan != null) {
            context.put("projectDescription", plan.getDescriptionProjet());
            context.put("projectObjective", plan.getObjectifCredit());
            context.put("investmentAmount", plan.getMontantInvestissement());
            context.put("expectedProjectRevenue", plan.getRevenuMensuelPrevu());
            context.put("expectedProjectProfit", plan.getProfitMensuelPrevu());
            context.put("profitabilityDelayMonths", plan.getDelaiRentabiliteMois());
            context.put("natureActivite", plan.getNatureActivite().name());
        }

        if (modalite != null) {
            context.put("recommendedModalite", modalite.getModaliteRecommandee());
            context.put("chosenModalite", modalite.getModaliteChoisie());
            context.put("monthlyCapacity", modalite.getCapaciteMensuelleMax());
            context.put("amortizingMonthly", modalite.getMensualiteAmortissable());
            context.put("inFineMonthly", modalite.getMensualiteInFine());
            context.put("paymentToIncome", modalite.getPaymentToIncome());
            context.put("financialStress", modalite.getFinancialStress());
        }

        return context;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callFlaskAdvisor(Map<String, Object> request) {
        try {
            Map<String, Object> response = restTemplate.postForObject(
                    flaskBaseUrl + "/advisor/scenarios",
                    request,
                    Map.class
            );

            if (response == null) {
                throw new IllegalStateException("Empty response from Flask advisor service");
            }

            return response;
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to call Flask advisor service: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void addOllamaNarrative(Map<String, Object> advisorResponse, Map<String, Object> sourceRequest) {
        String narrative = callOllamaSafely(advisorResponse, sourceRequest);

        Object reportObj = advisorResponse.get("clientReport");
        Map<String, Object> report;

        if (reportObj instanceof Map<?, ?> existingReport) {
            report = (Map<String, Object>) existingReport;
        } else {
            report = new HashMap<>();
            advisorResponse.put("clientReport", report);
        }

        report.put("aiNarrative", narrative);
    }

    private String callOllamaSafely(Map<String, Object> advisorResponse, Map<String, Object> sourceRequest) {
        try {
            String prompt = buildOllamaPrompt(advisorResponse, sourceRequest);

            Map<String, Object> ollamaRequest = new LinkedHashMap<>();
            ollamaRequest.put("model", ollamaModel);
            ollamaRequest.put("prompt", prompt);
            ollamaRequest.put("stream", false);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    ollamaBaseUrl + "/api/generate",
                    ollamaRequest,
                    Map.class
            );

            if (response == null || response.get("response") == null) {
                return fallbackNarrative(advisorResponse);
            }

            return response.get("response").toString().trim();
        } catch (Exception ex) {
            return fallbackNarrative(advisorResponse);
        }
    }

    private String buildOllamaPrompt(Map<String, Object> advisorResponse, Map<String, Object> sourceRequest)
            throws JsonProcessingException {
        return """
                Write a client-facing financial advisory report in English.
                
                Important rules:
                - Do not mention machine learning, XGBoost, Monte Carlo, VaR, CVaR, or PD.
                - Use simple but professional financial language.
                - Explain the current situation.
                - Explain the best improvement path.
                - Explain practical actions the client should take.
                - Be precise and do not invent numbers.
                - Keep it between 180 and 260 words.
                
                Data:
                %s
                
                Original request context:
                %s
                """.formatted(
                objectMapper.writeValueAsString(advisorResponse),
                objectMapper.writeValueAsString(sourceRequest)
        );
    }

    @SuppressWarnings("unchecked")
    private String fallbackNarrative(Map<String, Object> advisorResponse) {
        Object reportObj = advisorResponse.get("clientReport");

        if (reportObj instanceof Map<?, ?> report) {
            Object mainRecommendation = report.get("mainRecommendation");
            if (mainRecommendation != null) {
                return "Your financial profile has been reviewed through several improvement paths. The strongest recommendation is: "
                        + mainRecommendation
                        + " This action can help improve your credit readiness and make your future request stronger.";
            }
        }

        return "Your financial profile has been reviewed through several improvement paths. The objective is to strengthen repayment capacity, reduce financial pressure, and prepare a safer future credit request.";
    }
}

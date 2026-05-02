package tn.esprit.pi_back.constants;

public class MLFeatures {

    // --- Segmentation Model Features (ClientSegmentationService) ---
    public static final String AGE = "age";
    public static final String INCOME = "income";
    public static final String PREMIUM = "premium_amount";
    public static final String TENURE = "policy_tenure_months";
    public static final String MISSED = "num_missed_payments_12m";
    public static final String DELAY = "avg_payment_delay_days";
    public static final String CLAIMS = "claims_frequency";
    public static final String CREDIT = "credit_score";
    public static final String CONSISTENCY = "payment_consistency_score";
    public static final String ACCOUNT = "account_age_months";
    public static final String TYPE = "policy_type";
    public static final String LOCATION = "location";

    // --- Credit Risk Model Features (EvaluationRisqueServiceImpl) ---
    public static final String PERSON_AGE = "person_age";
    public static final String PERSON_INCOME = "person_income";
    public static final String PERSON_HOME_OWNERSHIP = "person_home_ownership";
    public static final String PERSON_EMP_LENGTH = "person_emp_length";
    public static final String CB_PERSON_DEFAULT_ON_FILE = "cb_person_default_on_file";
    public static final String CB_PERSON_CRED_HIST_LENGTH = "cb_person_cred_hist_length";
    public static final String LOAN_INTENT = "loan_intent";
    public static final String LOAN_GRADE = "loan_grade";
    public static final String LOAN_AMNT = "loan_amnt";
    public static final String LOAN_INT_RATE = "loan_int_rate";
    public static final String LOAN_PERCENT_INCOME = "loan_percent_income";
    public static final String N_SIM = "n_sim";
    public static final String NOISE_FACTOR = "noise_factor";
}

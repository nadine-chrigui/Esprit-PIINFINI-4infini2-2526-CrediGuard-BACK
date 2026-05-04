package tn.esprit.pi_back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.pi_back.services.IInsurancePolicyService;

@Component
@RequiredArgsConstructor
public class InsuranceCronJobs {

    private final IInsurancePolicyService policyService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void checkExpiringPolicies() {
        policyService.checkExpiringPolicies();
    }
}

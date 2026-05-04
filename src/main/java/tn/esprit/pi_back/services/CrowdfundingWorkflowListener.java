package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.CrowdfundingProject;
import tn.esprit.pi_back.entities.Investment;
import tn.esprit.pi_back.entities.InvestmentPayment;
import tn.esprit.pi_back.entities.InvestorAnalyticsSnapshot;
import tn.esprit.pi_back.events.InvestmentPaymentConfirmedEvent;
import tn.esprit.pi_back.repositories.CrowdfundingProjectRepository;
import tn.esprit.pi_back.repositories.InvestmentPaymentRepository;
import tn.esprit.pi_back.repositories.InvestmentRepository;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class CrowdfundingWorkflowListener {

    private final InvestmentPaymentRepository investmentPaymentRepository;
    private final InvestmentRepository investmentRepository;
    private final CrowdfundingProjectRepository projectRepository;
    private final InvestmentScheduleService investmentScheduleService;
    private final InvestorAnalyticsService investorAnalyticsService;
    private final EmailService emailService;
    private final CrowdfundingSuccessPredictionService predictionService;

    @EventListener
    @Transactional
    public void onInvestmentPaymentConfirmed(InvestmentPaymentConfirmedEvent event) {
        InvestmentPayment payment = investmentPaymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new IllegalArgumentException("Investment payment not found"));

        if (payment.getInvestment() != null) {
            return;
        }

        Investment investment = new Investment();
        investment.setAmount(payment.getAmount());
        investment.setInvestmentDate(LocalDate.now());
        investment.setExpectedReturn(payment.getExpectedReturnSnapshot());
        investment.setStatus(Investment.InvestmentStatus.ACTIVE);
        investment.setInvestor(payment.getInvestor());
        investment.setProject(payment.getProject());
        investment = investmentRepository.save(investment);

        payment.setInvestment(investment);

        CrowdfundingProject project = payment.getProject();
        project.setCollectedAmount(round(project.getCollectedAmount() + payment.getAmount()));
        if (project.getCollectedAmount() >= project.getFundingGoal()) {
            project.setStatus(CrowdfundingProject.ProjectStatus.FUNDED);
        }
        projectRepository.save(project);
        investmentPaymentRepository.save(payment);

        investmentScheduleService.generateSchedule(
                investment,
                payment.getDurationYears(),
                payment.getScheduleFrequency(),
                payment.getInterestRateSnapshot()
        );

        InvestorAnalyticsSnapshot snapshot = investorAnalyticsService.generateSnapshot(investment);
        predictionService.refreshPrediction(project.getProjectId());
        notifyProjectOwner(project, snapshot);
    }

    private void notifyProjectOwner(CrowdfundingProject project, InvestorAnalyticsSnapshot snapshot) {
        if (project.getOwner() == null || project.getOwner().getEmail() == null || project.getOwner().getEmail().isBlank()) {
            return;
        }

        String subject = "New validated investment on project " + project.getTitle();
        String body = """
                A new investment has been validated through Stripe.

                Investor analysis:
                - Total invested: %s
                - Average investment: %s
                - Total returns received: %s
                - ROI: %s%%
                - Projects funded: %s
                - Classification: %s
                """.formatted(
                snapshot.getTotalInvested(),
                snapshot.getAverageInvestment(),
                snapshot.getTotalReturnsReceived(),
                snapshot.getRoi(),
                snapshot.getFundedProjectsCount(),
                snapshot.getClassification()
        );

        try {
            emailService.sendEmail(project.getOwner().getEmail(), subject, body);
        } catch (RuntimeException ex) {
            log.warn("Unable to send project owner notification for project {}", project.getProjectId(), ex);
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.InvestorAnalytics.InvestorAnalyticsResponse;
import tn.esprit.pi_back.entities.Investment;
import tn.esprit.pi_back.entities.InvestorAnalyticsSnapshot;
import tn.esprit.pi_back.entities.ReturnPayment;
import tn.esprit.pi_back.entities.enums.InvestorClassification;
import tn.esprit.pi_back.repositories.InvestmentRepository;
import tn.esprit.pi_back.repositories.InvestorAnalyticsSnapshotRepository;
import tn.esprit.pi_back.repositories.ReturnPaymentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestorAnalyticsServiceImpl implements InvestorAnalyticsService {

    private final InvestmentRepository investmentRepository;
    private final ReturnPaymentRepository returnPaymentRepository;
    private final InvestorAnalyticsSnapshotRepository snapshotRepository;

    @Override
    public InvestorAnalyticsSnapshot generateSnapshot(Investment investment) {
        List<Investment> investments = investmentRepository.findByInvestorId(investment.getInvestor().getId());
        double totalInvested = investments.stream().mapToDouble(Investment::getAmount).sum();
        double averageInvestment = investments.isEmpty() ? 0.0 : totalInvested / investments.size();
        double totalReturnsReceived = returnPaymentRepository
                .findByInvestmentInvestorIdAndStatus(investment.getInvestor().getId(), ReturnPayment.ReturnStatus.PAID)
                .stream()
                .mapToDouble(ReturnPayment::getAmount)
                .sum();
        long fundedProjectsCount = investments.stream()
                .map(inv -> inv.getProject().getProjectId())
                .distinct()
                .count();

        double roi = totalInvested <= 0.0 ? 0.0 : ((totalReturnsReceived - totalInvested) / totalInvested) * 100.0;
        double concentrationRatio = totalInvested <= 0.0 ? 0.0 : averageInvestment / totalInvested;
        InvestorClassification classification = classifyInvestor(roi, totalInvested, fundedProjectsCount, concentrationRatio);

        InvestorAnalyticsSnapshot snapshot = snapshotRepository
                .findTopByInvestmentInvestmentIdOrderByCreatedAtDesc(investment.getInvestmentId())
                .orElseGet(InvestorAnalyticsSnapshot::new);

        snapshot.setInvestor(investment.getInvestor());
        snapshot.setProject(investment.getProject());
        snapshot.setInvestment(investment);
        snapshot.setTotalInvested(round(totalInvested));
        snapshot.setAverageInvestment(round(averageInvestment));
        snapshot.setTotalReturnsReceived(round(totalReturnsReceived));
        snapshot.setRoi(round(roi));
        snapshot.setFundedProjectsCount(fundedProjectsCount);
        snapshot.setClassification(classification);
        return snapshotRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public InvestorAnalyticsResponse getLatestByInvestor(Long investorId) {
        return snapshotRepository.findTopByInvestorIdOrderByCreatedAtDesc(investorId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("No analytics found for investor"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestorAnalyticsResponse> getHistoryByInvestor(Long investorId) {
        return snapshotRepository.findByInvestorIdOrderByCreatedAtDesc(investorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InvestorClassification classifyInvestor(double roi, double totalInvested, long fundedProjectsCount, double concentrationRatio) {
        if (roi >= 25.0 && fundedProjectsCount >= 4 && totalInvested >= 5000.0 && concentrationRatio <= 0.4) {
            return InvestorClassification.PREMIUM;
        }
        if (roi >= 10.0 && roi < 25.0 && fundedProjectsCount >= 2) {
            return InvestorClassification.PROFITABLE;
        }
        if (roi >= 0.0 && roi < 10.0 && fundedProjectsCount >= 2 && concentrationRatio < 0.5) {
            return InvestorClassification.STABLE;
        }
        return InvestorClassification.RISKY;
    }

    private InvestorAnalyticsResponse toResponse(InvestorAnalyticsSnapshot snapshot) {
        return new InvestorAnalyticsResponse(
                snapshot.getId(),
                snapshot.getInvestor().getId(),
                snapshot.getProject().getProjectId(),
                snapshot.getInvestment().getInvestmentId(),
                snapshot.getTotalInvested(),
                snapshot.getAverageInvestment(),
                snapshot.getTotalReturnsReceived(),
                snapshot.getRoi(),
                snapshot.getFundedProjectsCount(),
                snapshot.getClassification(),
                snapshot.getCreatedAt()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Projection.ProjectProjectionRequest;
import tn.esprit.pi_back.dto.Projection.ProjectProjectionResponse;
import tn.esprit.pi_back.entities.CrowdfundingProject;
import tn.esprit.pi_back.entities.ProjectFinancialProjection;
import tn.esprit.pi_back.entities.PurchaseOption;
import tn.esprit.pi_back.repositories.CrowdfundingProjectRepository;
import tn.esprit.pi_back.repositories.ProjectFinancialProjectionRepository;
import tn.esprit.pi_back.repositories.PurchaseOptionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectFinancialProjectionServiceImpl implements ProjectFinancialProjectionService {

    private final CrowdfundingProjectRepository projectRepository;
    private final PurchaseOptionRepository purchaseOptionRepository;
    private final ProjectFinancialProjectionRepository projectionRepository;

    @Override
    public ProjectProjectionResponse createProjection(ProjectProjectionRequest request) {
        CrowdfundingProject project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        PurchaseOption option = purchaseOptionRepository.findById(request.purchaseOptionId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase option not found"));

        if (!option.getProject().getProjectId().equals(project.getProjectId())) {
            throw new IllegalArgumentException("Purchase option does not belong to project");
        }

        double currentRevenue = option.getFixedPrice() * option.getMaxQuantity() * request.estimatedSalesRate();
        double futureRevenue = currentRevenue * Math.pow(1 + request.growthRate(), request.durationYears());
        double investorCost = project.getCollectedAmount() * Math.pow(1 + (project.getInterestRate() / 100.0), request.durationYears());
        double netProfit = futureRevenue - investorCost;

        ProjectFinancialProjection projection = new ProjectFinancialProjection();
        projection.setProject(project);
        projection.setPurchaseOption(option);
        projection.setTotalInvestment(round(project.getCollectedAmount()));
        projection.setInterestRate(round(project.getInterestRate()));
        projection.setOptionPrice(round(option.getFixedPrice()));
        projection.setQuantity(option.getMaxQuantity());
        projection.setSoldQuantity(option.getSoldQuantity());
        projection.setRemainingQuantity(option.getRemainingQuantity());
        projection.setEstimatedSalesRate(request.estimatedSalesRate());
        projection.setGrowthRate(request.growthRate());
        projection.setDurationYears(request.durationYears());
        projection.setCurrentRevenue(round(currentRevenue));
        projection.setFutureRevenue(round(futureRevenue));
        projection.setInvestorCost(round(investorCost));
        projection.setNetProfit(round(netProfit));
        projection.setSummaryMessage(buildSummary(request.durationYears(), futureRevenue, netProfit));

        return toResponse(projectionRepository.save(projection));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectProjectionResponse> getByProject(Long projectId) {
        return projectionRepository.findByProjectProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String buildSummary(Integer years, double futureRevenue, double netProfit) {
        return "Based on our financial analysis, your project is expected to generate approximately "
                + round(futureRevenue)
                + " over "
                + years
                + " years with a net profit of "
                + round(netProfit)
                + ".";
    }

    private ProjectProjectionResponse toResponse(ProjectFinancialProjection projection) {
        return new ProjectProjectionResponse(
                projection.getId(),
                projection.getProject().getProjectId(),
                projection.getPurchaseOption() != null ? projection.getPurchaseOption().getOptionId() : null,
                projection.getTotalInvestment(),
                projection.getInterestRate(),
                projection.getOptionPrice(),
                projection.getQuantity(),
                projection.getSoldQuantity(),
                projection.getRemainingQuantity(),
                projection.getEstimatedSalesRate(),
                projection.getGrowthRate(),
                projection.getDurationYears(),
                projection.getCurrentRevenue(),
                projection.getFutureRevenue(),
                projection.getInvestorCost(),
                projection.getNetProfit(),
                projection.getSummaryMessage(),
                projection.getCreatedAt()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

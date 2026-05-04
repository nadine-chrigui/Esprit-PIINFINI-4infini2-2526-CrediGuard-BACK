package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Investment.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final CrowdfundingProjectRepository projectRepository;

    @Override
    public InvestmentResponse create(InvestmentCreateRequest req) {
        User investor = userRepository.findById(req.investorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CrowdfundingProject project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Investment investment = new Investment();
        investment.setAmount(req.amount());
        investment.setInvestmentDate(req.investmentDate());
        investment.setExpectedReturn(req.expectedReturn());
        investment.setInvestor(investor);
        investment.setProject(project);

        // Update project collected amount
        project.setCollectedAmount(project.getCollectedAmount() + req.amount());

        return map(investmentRepository.save(investment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentResponse> getAll() {
        return investmentRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentResponse getById(Long id) {
        return map(investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentResponse> getByInvestor(Long investorId) {
        return investmentRepository.findByInvestorId(investorId).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentResponse> getByProject(Long projectId) {
        return investmentRepository.findByProjectProjectId(projectId).stream().map(this::map).toList();
    }

    @Override
    public InvestmentResponse update(Long id, InvestmentUpdateRequest req) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
        if (req.status() != null) investment.setStatus(req.status());
        return map(investment);
    }

    @Override
    public void delete(Long id) {
        investmentRepository.deleteById(id);
    }

    private InvestmentResponse map(Investment i) {
        return new InvestmentResponse(
                i.getInvestmentId(),
                i.getAmount(),
                i.getInvestmentDate(),
                i.getExpectedReturn(),
                i.getStatus().name(),
                i.getInvestor().getId(),
                i.getProject().getProjectId()
        );
    }
}

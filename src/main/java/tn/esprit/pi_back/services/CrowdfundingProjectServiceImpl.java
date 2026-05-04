package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Crowdfunding.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CrowdfundingProjectServiceImpl implements CrowdfundingProjectService {

    private final CrowdfundingProjectRepository repo;
    private final UserRepository userRepo;

    @Override
    public CrowdfundingResponse create(CrowdfundingCreateRequest req) {

        User owner = userRepo.findById(req.ownerId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CrowdfundingProject p = new CrowdfundingProject();

        p.setTitle(req.title().trim());
        p.setDescription(req.description());
        p.setFundingGoal(req.fundingGoal());
        p.setInterestRate(req.interestRate());
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());
        p.setOwner(owner);
        // Visible to investors on the public list (ACTIVE/FUNDED filter). Use DRAFT + approval flow later if needed.
        p.setStatus(CrowdfundingProject.ProjectStatus.ACTIVE);

        CrowdfundingProject saved = repo.save(p);

        return map(saved);
    }

    @Override
    public List<CrowdfundingResponse> getAll() {
        return repo.findAll().stream().map(this::map).toList();
    }

    @Override
    public CrowdfundingResponse getById(Long id) {
        return map(repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found")));
    }

    @Override
    public CrowdfundingResponse update(Long id, CrowdfundingUpdateRequest req) {

        CrowdfundingProject p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (req.title() != null) {
            p.setTitle(req.title().trim());
        }
        if (req.description() != null) {
            p.setDescription(req.description());
        }
        if (req.fundingGoal() != null) {
            p.setFundingGoal(req.fundingGoal());
        }
        if (req.interestRate() != null) {
            p.setInterestRate(req.interestRate());
        }
        if (req.startDate() != null) {
            p.setStartDate(req.startDate());
        }
        if (req.endDate() != null) {
            p.setEndDate(req.endDate());
        }

        CrowdfundingProject saved = repo.save(p);
        return map(saved);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private CrowdfundingResponse map(CrowdfundingProject p) {
        return new CrowdfundingResponse(
                p.getProjectId(),
                p.getTitle(),
                p.getDescription(),
                p.getFundingGoal(),
                p.getCollectedAmount(),
                p.getInterestRate(),
                p.getStartDate(),
                p.getEndDate(),
                p.getStatus().name(),
                p.getOwner().getId()
        );
    }
}
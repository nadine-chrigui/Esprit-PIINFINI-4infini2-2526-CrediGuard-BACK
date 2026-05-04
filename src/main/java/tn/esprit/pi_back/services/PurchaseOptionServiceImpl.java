package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.PurchaseOption.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOptionServiceImpl implements PurchaseOptionService {

    private final PurchaseOptionRepository purchaseOptionRepository;
    private final CrowdfundingProjectRepository projectRepository;

    @Override
    public PurchaseOptionResponse create(PurchaseOptionCreateRequest req) {
        CrowdfundingProject project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        PurchaseOption option = new PurchaseOption();
        option.setFixedPrice(req.fixedPrice());
        option.setMaxQuantity(req.maxQuantity());
        option.setSoldQuantity(0);
        option.setRemainingQuantity(req.maxQuantity());
        option.setCommissionRate(req.commissionRate());
        option.setExpirationDate(req.expirationDate());
        option.setProject(project);

        return map(purchaseOptionRepository.save(option));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOptionResponse> getAll() {
        return purchaseOptionRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOptionResponse getById(Long id) {
        return map(purchaseOptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOption not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOptionResponse> getByProject(Long projectId) {
        return purchaseOptionRepository.findByProjectProjectId(projectId)
                .stream().map(this::map).toList();
    }

    @Override
    public PurchaseOptionResponse update(Long id, PurchaseOptionUpdateRequest req) {
        PurchaseOption option = purchaseOptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PurchaseOption not found"));

        if (req.fixedPrice() != null) option.setFixedPrice(req.fixedPrice());
        if (req.maxQuantity() != null) {
            if (req.maxQuantity() < option.getSoldQuantity()) {
                throw new IllegalArgumentException("maxQuantity cannot be lower than soldQuantity");
            }
            option.setMaxQuantity(req.maxQuantity());
            option.setRemainingQuantity(req.maxQuantity() - option.getSoldQuantity());
        }
        if (req.commissionRate() != null) option.setCommissionRate(req.commissionRate());
        if (req.expirationDate() != null) option.setExpirationDate(req.expirationDate());
        if (req.status() != null) option.setStatus(req.status());

        if (option.getRemainingQuantity() == 0 && option.getStatus() == PurchaseOption.OptionStatus.ACTIVE) {
            option.setStatus(PurchaseOption.OptionStatus.SOLD_OUT);
        }

        return map(option);
    }

    @Override
    public void delete(Long id) {
        purchaseOptionRepository.deleteById(id);
    }

    private PurchaseOptionResponse map(PurchaseOption o) {
        return new PurchaseOptionResponse(
                o.getOptionId(),
                o.getFixedPrice(),
                o.getMaxQuantity(),
                o.getSoldQuantity(),
                o.getRemainingQuantity(),
                o.getCommissionRate(),
                o.getExpirationDate(),
                o.getStatus().name(),
                o.getProject().getProjectId()
        );
    }
}

package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Crowdfunding.*;
import java.util.List;

public interface CrowdfundingProjectService {
    CrowdfundingResponse create(CrowdfundingCreateRequest req);
    List<CrowdfundingResponse> getAll();
    CrowdfundingResponse getById(Long id);
    CrowdfundingResponse update(Long id, CrowdfundingUpdateRequest req);
    void delete(Long id);
}

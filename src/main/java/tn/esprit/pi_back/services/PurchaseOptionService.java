package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.PurchaseOption.*;
import java.util.List;

public interface PurchaseOptionService {
    PurchaseOptionResponse create(PurchaseOptionCreateRequest req);
    List<PurchaseOptionResponse> getAll();
    PurchaseOptionResponse getById(Long id);
    List<PurchaseOptionResponse> getByProject(Long projectId);
    PurchaseOptionResponse update(Long id, PurchaseOptionUpdateRequest req);
    void delete(Long id);
}

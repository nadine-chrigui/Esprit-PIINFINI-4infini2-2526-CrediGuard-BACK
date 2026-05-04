package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.OptionRedemption.*;
import java.util.List;

public interface OptionRedemptionService {
    OptionRedemptionResponse create(OptionRedemptionCreateRequest req);
    List<OptionRedemptionResponse> getAll();
    OptionRedemptionResponse getById(Long id);
    OptionRedemptionResponse getBySubscription(Long subscriptionId);
    void delete(Long id);
}

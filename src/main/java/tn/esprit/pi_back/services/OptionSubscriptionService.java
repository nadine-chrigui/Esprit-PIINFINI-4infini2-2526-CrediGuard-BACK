package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.OptionSubscription.*;
import java.util.List;

public interface OptionSubscriptionService {
    OptionSubscriptionResponse create(OptionSubscriptionCreateRequest req);
    List<OptionSubscriptionResponse> getAll();
    OptionSubscriptionResponse getById(Long id);
    List<OptionSubscriptionResponse> getByUser(Long userId);
    List<OptionSubscriptionResponse> getByOption(Long optionId);
    OptionSubscriptionResponse update(Long id, OptionSubscriptionUpdateRequest req);
    void delete(Long id);
}

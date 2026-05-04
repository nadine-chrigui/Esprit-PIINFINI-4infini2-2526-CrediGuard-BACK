package tn.esprit.pi_back.dto.OptionSubscription;

import tn.esprit.pi_back.entities.OptionSubscription;

public record OptionSubscriptionUpdateRequest(
        OptionSubscription.SubscriptionStatus status
) {}

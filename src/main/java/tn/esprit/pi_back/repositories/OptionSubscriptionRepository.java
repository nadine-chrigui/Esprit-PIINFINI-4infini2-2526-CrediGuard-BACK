package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.OptionSubscription;

import java.util.List;

@Repository
public interface OptionSubscriptionRepository extends JpaRepository<OptionSubscription, Long> {
    List<OptionSubscription> findByUserId(Long userId);
    List<OptionSubscription> findByPurchaseOptionOptionId(Long optionId);
    List<OptionSubscription> findByStatus(OptionSubscription.SubscriptionStatus status);
    List<OptionSubscription> findByPurchaseOptionOptionIdAndStatusNot(Long optionId, OptionSubscription.SubscriptionStatus status);
}

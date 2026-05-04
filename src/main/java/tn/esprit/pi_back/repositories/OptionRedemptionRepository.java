package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.OptionRedemption;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRedemptionRepository extends JpaRepository<OptionRedemption, Long> {
    Optional<OptionRedemption> findBySubscriptionSubscriptionId(Long subscriptionId);
    List<OptionRedemption> findByOrderId(Long orderId);
}

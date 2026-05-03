package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.LoyaltyHistory;

import java.util.List;

public interface LoyaltyHistoryRepository extends JpaRepository<LoyaltyHistory, Long> {
    List<LoyaltyHistory> findByLoyaltyAccountIdOrderByChangeDateDesc(Long loyaltyAccountId);
}
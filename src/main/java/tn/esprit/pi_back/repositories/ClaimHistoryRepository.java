package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.ClaimHistory;
import java.util.List;

@Repository
public interface ClaimHistoryRepository extends JpaRepository<ClaimHistory, Long> {
    List<ClaimHistory> findByClaimIdOrderByChangedAtAsc(Long claimId);
}

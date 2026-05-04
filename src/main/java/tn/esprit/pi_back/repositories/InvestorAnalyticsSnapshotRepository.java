package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.InvestorAnalyticsSnapshot;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestorAnalyticsSnapshotRepository extends JpaRepository<InvestorAnalyticsSnapshot, Long> {
    List<InvestorAnalyticsSnapshot> findByInvestorIdOrderByCreatedAtDesc(Long investorId);
    Optional<InvestorAnalyticsSnapshot> findTopByInvestorIdOrderByCreatedAtDesc(Long investorId);
    Optional<InvestorAnalyticsSnapshot> findTopByInvestmentInvestmentIdOrderByCreatedAtDesc(Long investmentId);
}

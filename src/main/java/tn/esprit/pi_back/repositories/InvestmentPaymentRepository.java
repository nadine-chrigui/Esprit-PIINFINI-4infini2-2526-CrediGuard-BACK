package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.InvestmentPayment;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentPaymentRepository extends JpaRepository<InvestmentPayment, Long> {
    Optional<InvestmentPayment> findByStripePaymentIntentId(String stripePaymentIntentId);
    List<InvestmentPayment> findByInvestorIdOrderByCreatedAtDesc(Long investorId);
}

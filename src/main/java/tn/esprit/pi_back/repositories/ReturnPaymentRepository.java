package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.ReturnPayment;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnPaymentRepository extends JpaRepository<ReturnPayment, Long> {
    List<ReturnPayment> findByInvestmentInvestmentId(Long investmentId);
    List<ReturnPayment> findByStatus(ReturnPayment.ReturnStatus status);
    List<ReturnPayment> findByType(ReturnPayment.ReturnType type);
    List<ReturnPayment> findByInvestmentInvestorIdAndStatus(Long investorId, ReturnPayment.ReturnStatus status);
    Optional<ReturnPayment> findByStripePaymentIntentId(String stripePaymentIntentId);
}

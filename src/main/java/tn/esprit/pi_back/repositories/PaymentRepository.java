package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.Payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrderId(Long orderId);
    Optional<Payment> findByOrderId(Long orderId);
    @Query("""
    select p.paymentType, count(p), coalesce(sum(p.amount), 0)
    from Payment p
    group by p.paymentType
    order by count(p) desc
""")
    List<Object[]> countAndSumByPaymentType();

}
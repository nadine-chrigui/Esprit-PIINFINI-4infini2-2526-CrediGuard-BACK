package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.InvestmentOffer;
import tn.esprit.pi_back.entities.enums.OfferStatus;

import java.util.List;

public interface InvestmentOfferRepository extends JpaRepository<InvestmentOffer, Long> {
    List<InvestmentOffer> findByUserId(Long userId);
    List<InvestmentOffer> findByUserIdAndStatus(Long userId, OfferStatus status);
}
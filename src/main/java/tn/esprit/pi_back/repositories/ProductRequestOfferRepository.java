package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.ProductRequestOffer;
import tn.esprit.pi_back.entities.enums.ProductRequestOfferStatus;

import java.util.List;
import java.util.Optional;

public interface ProductRequestOfferRepository extends JpaRepository<ProductRequestOffer, Long> {

    List<ProductRequestOffer> findByProductRequestIdOrderByCreatedAtDesc(Long productRequestId);

    List<ProductRequestOffer> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<ProductRequestOffer> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, ProductRequestOfferStatus status);

    List<ProductRequestOffer> findByProductRequestId(Long productRequestId);

    Optional<ProductRequestOffer> findById(Long id);
    boolean existsByProductRequestIdAndSellerIdAndStatus(
            Long productRequestId,
            Long sellerId,
            ProductRequestOfferStatus status
    );

}
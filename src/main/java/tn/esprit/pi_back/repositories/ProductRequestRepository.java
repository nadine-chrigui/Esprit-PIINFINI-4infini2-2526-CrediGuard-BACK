package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.ProductRequest;
import tn.esprit.pi_back.entities.enums.ProductRequestStatus;

import java.util.List;
import java.util.Optional;

public interface ProductRequestRepository extends JpaRepository<ProductRequest, Long> {

    List<ProductRequest> findByClientIdOrderByCreatedAtDesc(Long clientId);

    long countByStatus(ProductRequestStatus status);

    List<ProductRequest> findByStatusOrderByCreatedAtDesc(ProductRequestStatus status);

    List<ProductRequest> findByTargetSellerIdOrderByCreatedAtDesc(Long sellerId);

    List<ProductRequest> findByStatusAndTargetSellerIsNullOrderByCreatedAtDesc(ProductRequestStatus status);

    Optional<ProductRequest> findByIdAndClientId(Long id, Long clientId);
}

package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Optional<Product> findByIdAndSellerIdAndActiveTrue(Long id, Long sellerId);

}
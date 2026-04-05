package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Optional<Product> findByIdAndSellerIdAndActiveTrue(Long id, Long sellerId);

    @Query("""
        select p.id, p.name, p.seller.fullName, p.category.name, p.stockQuantity
        from Product p
        where p.active = true
          and p.saleType = tn.esprit.pi_back.entities.enums.SaleMode.STANDARD
          and p.stockQuantity is not null
          and p.stockQuantity <= :threshold
        order by p.stockQuantity asc, p.name asc
    """)
    List<Object[]> findLowStockProducts(@Param("threshold") Integer threshold);
}
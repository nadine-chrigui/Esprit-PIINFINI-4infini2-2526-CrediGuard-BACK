package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.PartnerProduct;

import java.util.List;

public interface PartnerProductRepository extends JpaRepository<PartnerProduct, Long> {

    List<PartnerProduct> findByPartnerId(Long partnerId);
}
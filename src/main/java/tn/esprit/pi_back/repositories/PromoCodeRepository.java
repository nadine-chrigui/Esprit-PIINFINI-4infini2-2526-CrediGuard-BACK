package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.PromoCode;

import java.util.Optional;


public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
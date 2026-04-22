package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.Voucher;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // 🔥 AJOUTE ÇA
    Optional<Voucher> findByCode(String code);
}
package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.pi_back.entities.Promotion;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("""
        select p from Promotion p
        where p.active = true
          and p.autoApply = true
        order by p.priority desc, p.id desc
    """)
    List<Promotion> findAllEnabled();
}
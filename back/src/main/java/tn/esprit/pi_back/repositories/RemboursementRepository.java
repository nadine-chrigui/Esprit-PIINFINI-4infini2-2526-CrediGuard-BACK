package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.Remboursement;

import java.util.List;

public interface RemboursementRepository extends JpaRepository<Remboursement, Long> {

    List<Remboursement> findByCreditId(Long creditId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Remboursement r where r.transaction is not null and r.transaction.idTransaction in :transactionIds")
    int deleteByTransactionIds(@Param("transactionIds") List<Long> transactionIds);
}

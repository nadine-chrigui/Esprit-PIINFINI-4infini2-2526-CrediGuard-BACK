package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.ProfilCredit;

import java.util.List;
import java.util.Optional;

public interface ProfilCreditRepository extends JpaRepository<ProfilCredit, Long> {

    @Query("SELECT p FROM ProfilCredit p JOIN p.client c WHERE c.email = :email")
    Optional<ProfilCredit> findByClientEmail(@Param("email") String email);

    @Query("SELECT p FROM ProfilCredit p JOIN p.client c WHERE c.id = :clientId")
    Optional<ProfilCredit> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProfilCredit p JOIN p.client c WHERE c.id = :clientId")
    boolean existsByClientId(@Param("clientId") Long clientId);

    @Query("SELECT p FROM ProfilCredit p JOIN p.client c WHERE c.id = :clientId")
    List<ProfilCredit> retrieveProfilsByClientId(@Param("clientId") Long clientId);
}

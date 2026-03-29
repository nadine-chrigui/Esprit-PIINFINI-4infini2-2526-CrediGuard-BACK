package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.TransportBooking;

@Repository
public interface TransportBookingRepository extends JpaRepository<TransportBooking, Long> {
}

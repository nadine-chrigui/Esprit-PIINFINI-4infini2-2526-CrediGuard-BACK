package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tn.esprit.pi_back.entities.TransportBooking;

import java.util.List;

@RepositoryRestResource(path = "transport-bookings")
public interface TransportBookingRepository extends JpaRepository<TransportBooking, Long> {

    List<TransportBooking> findByBeneficiaryId(Long beneficiaryId);

    List<TransportBooking> findByTransportServiceId(Long transportServiceId);
}
package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.DeliveryAddress;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
}
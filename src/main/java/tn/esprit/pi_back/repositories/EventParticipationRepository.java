package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tn.esprit.pi_back.entities.EventParticipation;

import java.util.List;

@RepositoryRestResource(path = "event-participations")
public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {

    List<EventParticipation> findByBeneficiaryId(Long beneficiaryId);

    List<EventParticipation> findByEventId(Long eventId);
}
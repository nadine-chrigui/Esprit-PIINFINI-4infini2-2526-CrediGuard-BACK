package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.insurance.InsuranceOfferDTO;
import tn.esprit.pi_back.dto.insurance.RecommendedOfferDTO;
import tn.esprit.pi_back.entities.InsuranceOffer;
import java.util.List;

public interface IInsuranceOfferService {
    List<InsuranceOfferDTO> getAll();
    List<RecommendedOfferDTO> getRecommended(Long clientId);
    InsuranceOffer save(InsuranceOffer offer);
    InsuranceOffer update(Long id, InsuranceOffer offer);
    void delete(Long id);
    InsuranceOffer getById(Long id);
}

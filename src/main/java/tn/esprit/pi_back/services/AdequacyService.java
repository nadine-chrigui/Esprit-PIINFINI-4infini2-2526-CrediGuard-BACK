package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.InsuranceOffer;
import tn.esprit.pi_back.entities.User;

public interface AdequacyService {
    int calculateScore(InsuranceOffer offer, User client);
}

package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.User;

import java.util.List;

public interface IInsuranceUserService {

    User addInsuranceUser(User u);

    User updateInsuranceUser(User u);

    void deleteInsuranceUser(Long id);

    User getInsuranceUser(Long id);

    List<User> getAllInsuranceUsers();
}
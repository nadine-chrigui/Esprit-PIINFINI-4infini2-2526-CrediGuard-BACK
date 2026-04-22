package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.InsuranceCompany;

import java.util.List;

public interface IInsuranceCompanyService {

    InsuranceCompany add(InsuranceCompany c);

    InsuranceCompany update(InsuranceCompany c);

    void delete(Long id);

    InsuranceCompany get(Long id);

    List<InsuranceCompany> all();
}
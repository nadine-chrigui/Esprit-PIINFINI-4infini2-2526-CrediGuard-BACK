package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.insurance.InsuranceCompanyDTO;
import tn.esprit.pi_back.entities.InsuranceCompany;
import java.util.List;

public interface IInsuranceCompanyService {
    List<InsuranceCompanyDTO> getAllPublic();
    InsuranceCompanyDTO getByIdWithOffers(Long id);
    InsuranceCompany save(InsuranceCompany company);
    InsuranceCompany update(Long id, InsuranceCompany company);
    void delete(Long id);
    InsuranceCompany getById(Long id);
    List<InsuranceCompany> getAll();
}
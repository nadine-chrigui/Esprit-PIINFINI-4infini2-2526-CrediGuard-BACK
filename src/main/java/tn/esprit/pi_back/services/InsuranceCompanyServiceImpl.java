package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.InsuranceCompany;
import tn.esprit.pi_back.repositories.InsuranceCompanyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceCompanyServiceImpl implements IInsuranceCompanyService {

    private final InsuranceCompanyRepository repo;

    @Override
    public InsuranceCompany add(InsuranceCompany c) {
        return repo.save(c);
    }

    @Override
    public InsuranceCompany update(InsuranceCompany c) {
        return repo.save(c);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public InsuranceCompany get(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public List<InsuranceCompany> all() {
        return repo.findAll();
    }
}
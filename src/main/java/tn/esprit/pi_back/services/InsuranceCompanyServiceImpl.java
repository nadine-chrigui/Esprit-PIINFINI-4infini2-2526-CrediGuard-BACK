package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.insurance.InsuranceCompanyDTO;
import tn.esprit.pi_back.dto.insurance.InsuranceMapper;
import tn.esprit.pi_back.dto.insurance.InsuranceOfferDTO;
import tn.esprit.pi_back.entities.InsuranceCompany;
import tn.esprit.pi_back.entities.InsuranceOffer;
import tn.esprit.pi_back.repositories.InsuranceCompanyRepository;
import tn.esprit.pi_back.repositories.InsuranceOfferRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsuranceCompanyServiceImpl implements IInsuranceCompanyService {

    private final InsuranceCompanyRepository companyRepository;
    private final InsuranceOfferRepository offerRepository;

    @Override
    public List<InsuranceCompanyDTO> getAllPublic() {
        return companyRepository.findAll().stream()
                .map(InsuranceMapper::toCompanyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InsuranceCompanyDTO getByIdWithOffers(Long id) {
        InsuranceCompany company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assureur non trouvé"));
        
        InsuranceCompanyDTO dto = InsuranceMapper.toCompanyDTO(company);
        
        List<InsuranceOffer> offers = offerRepository.findByInsuranceCompanyId(id);
        List<InsuranceOfferDTO> offerDtos = offers.stream()
                .map(InsuranceMapper::toOfferDTO)
                .collect(Collectors.toList());
        
        dto.setOffers(offerDtos);
        return dto;
    }

    @Override
    public InsuranceCompany save(InsuranceCompany company) {
        return companyRepository.save(company);
    }

    @Override
    public InsuranceCompany update(Long id, InsuranceCompany company) {
        InsuranceCompany existing = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assureur non trouvé"));
        
        existing.setName(company.getName());
        existing.setLogoUrl(company.getLogoUrl());
        existing.setDescription(company.getDescription());
        existing.setCategories(company.getCategories());
        existing.setReliabilityNote(company.getReliabilityNote());
        existing.setRegistrationNumber(company.getRegistrationNumber());
        
        return companyRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        InsuranceCompany company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assureur non trouvé"));
        company.setActive(false);
        companyRepository.save(company);
    }

    @Override
    public InsuranceCompany getById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    @Override
    public List<InsuranceCompany> getAll() {
        return companyRepository.findAll();
    }
}
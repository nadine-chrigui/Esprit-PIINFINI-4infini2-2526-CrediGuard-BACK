package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.PartnerProduct;
import tn.esprit.pi_back.repositories.PartnerProductRepository;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.entities.User;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PartnerProductServiceImpl implements IPartnerProductService {

    private final PartnerProductRepository repo;        // ✅ OK
    private final UserRepository userRepository;        // ✅ déplacé en haut

    @Override
    public List<PartnerProduct> getByPartner(Long partnerId) {
        return repo.findByPartnerId(partnerId);
    }

    @Override
    public PartnerProduct save(PartnerProduct product) {

        User partner = userRepository.findById(product.getPartner().getId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        product.setPartner(partner);

        return repo.save(product);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public List<PartnerProduct> getAll() {
        return repo.findAll(); // ✅ CORRIGÉ ICI
    }
}
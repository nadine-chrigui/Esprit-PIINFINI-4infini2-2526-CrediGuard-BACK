package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.PartnerProduct;
import java.util.List;

public interface IPartnerProductService {

    List<PartnerProduct> getByPartner(Long partnerId);

    PartnerProduct save(PartnerProduct product);

    void delete(Long id);
    List<PartnerProduct> getAll();
}
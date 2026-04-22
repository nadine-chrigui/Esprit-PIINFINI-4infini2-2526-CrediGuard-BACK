package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.insurance.UserMiniDTO;
import tn.esprit.pi_back.entities.User;
import java.util.List;

public interface IPartnerService {
    UserMiniDTO addPartner(User u);
    UserMiniDTO updatePartner(User u);
    void deletePartner(Long id);
    UserMiniDTO getPartner(Long id);
    List<UserMiniDTO> getAllPartners();
    long countPartners();
}
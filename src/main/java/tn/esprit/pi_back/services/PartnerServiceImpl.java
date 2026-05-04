package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.insurance.UserMiniDTO;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.repositories.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements IPartnerService {

    private final UserRepository userRepo;

    @Override
    public UserMiniDTO addPartner(User u) {
        u.setUserType(UserType.PARTNER);
        if (u.getEnabled() == null) u.setEnabled(true);
        return toDTO(userRepo.save(u));
    }

    @Override
    public UserMiniDTO updatePartner(User u) {
        u.setUserType(UserType.PARTNER);
        return toDTO(userRepo.save(u));
    }

    @Override
    public void deletePartner(Long id) {
        userRepo.deleteById(id);
    }

    @Override
    public UserMiniDTO getPartner(Long id) {
        User u = userRepo.findById(id).orElse(null);
        if (u == null || u.getUserType() != UserType.PARTNER) return null;
        return toDTO(u);
    }

    @Override
    public List<UserMiniDTO> getAllPartners() {
        return userRepo.findByUserType(UserType.PARTNER)
                .stream().map(this::toDTO).toList();
    }

    private UserMiniDTO toDTO(User u) {
        return new UserMiniDTO(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getPartnerType() // 🔥 IMPORTANT
        );
    }
    @Override
    public long countPartners() {
        return userRepo.countByUserType(UserType.PARTNER);
    }
}
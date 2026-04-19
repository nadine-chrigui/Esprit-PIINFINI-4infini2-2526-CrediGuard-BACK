package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.ProfileResponse;
import tn.esprit.pi_back.dto.UpdateProfileRequest;
import tn.esprit.pi_back.dto.UpdateUserRequest;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.PartnerType;
import java.util.List;

public interface UserService
{

        User create(User user);
        User update(Long id, UpdateUserRequest request);
        User getById(Long id);
        List<User> getAll();
        void delete(Long id);
        User getCurrentUserOrThrow();
        User getOrCreateCurrentUser();
        ProfileResponse getMyProfile();
        ProfileResponse updateMyProfile(UpdateProfileRequest request);
        List<User> getPartners();

        List<User> getPartnersByType(PartnerType type);
}

package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceUserServiceImpl implements IInsuranceUserService {

    private final UserRepository userRepository;

    @Override
    public User addInsuranceUser(User u) {
        u.setUserType(UserType.INSURANCE);
        if (u.getEnabled() == null) {
            u.setEnabled(true);
        }
        return userRepository.save(u);
    }

    @Override
    public User updateInsuranceUser(User u) {
        u.setUserType(UserType.INSURANCE);
        return userRepository.save(u);
    }

    @Override
    public void deleteInsuranceUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getInsuranceUser(Long id) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null || u.getUserType() != UserType.INSURANCE) {
            return null;
        }
        return u;
    }

    @Override
    public List<User> getAllInsuranceUsers() {
        return userRepository.findByUserType(UserType.INSURANCE);
    }
}
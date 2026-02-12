package tn.esprit.pi_back.Services;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.Entities.User;
import tn.esprit.pi_back.Repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        User existing = getById(id);

        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setUserType(user.getUserType());
        existing.setEnabled(user.isEnabled());


        // ✅ si l'utilisateur a fourni un nouveau password, on le hash
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existing);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        User existing = getById(id);
        userRepository.delete(existing);
    }
}
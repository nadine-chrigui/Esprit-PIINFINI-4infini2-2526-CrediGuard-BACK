package tn.esprit.pi_back.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
    @Override
    public User getOrCreateCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email;
        String fullName;

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            email = "test@test.tn";
            fullName = "Test User";
        } else {
            email = auth.getName();
            fullName = auth.getName();
        }

        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(email);
                    u.setFullName(fullName);
                    u.setUserType(UserType.CLIENT);  // ✅ adapte au bon enum
                    u.setPassword(passwordEncoder.encode("12345678"));
                    u.setEnabled(true); // si tu as ce champ
                    return userRepository.save(u);
                });
    }

    @Override
    public User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // ✅ DEV fallback READ-ONLY (aucun insert ici)
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByEmail("test@test.tn")
                    .orElseThrow(() -> new SecurityException(
                            "Unauthorized: no authenticated user. Create the test user first by calling POST /api/products once (or login)."
                    ));
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Unauthorized: user not found in DB"));
    }


}
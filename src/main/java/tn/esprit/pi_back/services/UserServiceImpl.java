package tn.esprit.pi_back.services;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.ProfileResponse;
import tn.esprit.pi_back.dto.UpdateProfileRequest;
import tn.esprit.pi_back.dto.UpdateUserRequest;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.repositories.UserRepository;

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
    public User update(Long id, UpdateUserRequest request) {
        User existing = getById(id);

        userRepository.findByEmail(request.getEmail()).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        });

        existing.setFullName(request.getFullName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setUserType(request.getUserType());
        existing.setEnabled(request.getEnabled());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
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
    public List<User> getAll(Boolean enabled, UserType userType) {
        if (enabled != null && userType != null) {
            return userRepository.findByEnabledAndUserTypeOrderByCreatedAtDesc(enabled, userType);
        }

        if (enabled != null) {
            return userRepository.findByEnabledOrderByCreatedAtDesc(enabled);
        }

        if (userType != null) {
            return userRepository.findByUserTypeOrderByCreatedAtDesc(userType);
        }

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
    @Override
    public ProfileResponse getMyProfile() {
        User user = getCurrentUserOrThrow();

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getUserType(),
                user.isEnabled()
        );
    }

    @Override
    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User user = getCurrentUserOrThrow();

        userRepository.findByEmail(request.getEmail()).ifPresent(found -> {
            if (!found.getId().equals(user.getId())) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        });

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        User saved = userRepository.save(user);

        return new ProfileResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getUserType(),
                saved.isEnabled()
        );
    }

    @Override
    public User updateEnabled(Long id, Boolean enabled) {
        if (enabled == null) {
            throw new IllegalArgumentException("enabled is required");
        }

        User existing = getById(id);
        User currentUser = getCurrentUserOrThrow();

        if (Boolean.FALSE.equals(enabled) && currentUser.getId().equals(existing.getId())) {
            throw new IllegalArgumentException("Admin cannot disable the currently authenticated account.");
        }

        existing.setEnabled(enabled);
        return userRepository.save(existing);
    }

}

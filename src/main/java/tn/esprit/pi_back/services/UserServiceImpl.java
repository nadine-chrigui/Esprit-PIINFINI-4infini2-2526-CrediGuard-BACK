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
                    u.setUserType(UserType.CLIENT);
                    u.setPassword(passwordEncoder.encode("12345678"));
                    u.setEnabled(true);
                    return userRepository.save(u);
                });
    }

    @Override
    public User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

        ProfileResponse resp = new ProfileResponse();
        resp.setId(user.getId());
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setUserType(user.getUserType());
        resp.setEnabled(user.isEnabled());
        resp.setSector(user.getSector());
        resp.setActivityType(user.getActivityType());
        resp.setRegion(user.getRegion());
        return resp;
    }

    @Override
    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User user = getOrCreateCurrentUser();

        // On ne change pas l'email en mode test pour éviter de casser la session/le fallback
        // user.setEmail(request.getEmail()); 
        
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setSector(request.getSector());
        user.setActivityType(request.getActivityType());
        user.setRegion(request.getRegion());

        // Sécurité : S'assurer que les champs vitaux ne sont pas nuls pour éviter les crashs de sauvegarde
        if (user.getEnabled() == null) user.setEnabled(true);
        if (user.getUserType() == null) user.setUserType(tn.esprit.pi_back.entities.enums.UserType.CLIENT);
        if (user.getPassword() == null) user.setPassword("12345678");

        User saved = userRepository.save(user);


        ProfileResponse resp = new ProfileResponse();
        resp.setId(saved.getId());
        resp.setFullName(saved.getFullName());
        resp.setEmail(saved.getEmail());
        resp.setPhone(saved.getPhone());
        resp.setUserType(saved.getUserType());
        resp.setEnabled(saved.isEnabled());
        resp.setSector(saved.getSector());
        resp.setActivityType(saved.getActivityType());
        resp.setRegion(saved.getRegion());
        return resp;
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
    @Override
    public List<User> getPartners() {
        return userRepository.findByUserType(UserType.PARTNER);
    }

    @Override
    public List<User> getPartnersByType(tn.esprit.pi_back.entities.enums.PartnerType partnerType) {
        return userRepository.findByUserType(UserType.PARTNER)
                .stream()
                .filter(u -> partnerType.equals(u.getPartnerType()))
                .toList();
    }
}
package tn.esprit.pi_back.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    List<User> findByUserType(UserType userType);
}
package tn.esprit.pi_back.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.Entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
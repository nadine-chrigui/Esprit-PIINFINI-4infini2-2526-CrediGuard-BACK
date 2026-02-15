package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.User;

import java.util.List;

public interface UserService
{

        User create(User user);
        User update(Long id, User user);
        User getById(Long id);
        List<User> getAll();
        void delete(Long id);
        User getCurrentUserOrThrow();
        User getOrCreateCurrentUser();
}


package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.ProfileResponse;
import tn.esprit.pi_back.dto.UpdateProfileRequest;
import tn.esprit.pi_back.dto.UpdateUserRequest;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class UserController
{
    private final UserService userService;

    // CREATE
    @PostMapping
    public ResponseEntity<User> create( @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.create(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }
    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }
    @GetMapping("/me-full")
    public ResponseEntity<User> getCurrentUserFull() {
        return ResponseEntity.ok(userService.getCurrentUserOrThrow());
    }
    @GetMapping("/partners")
    public ResponseEntity<List<User>> getPartners() {
        return ResponseEntity.ok(userService.getPartners());
    }

    @GetMapping("/partners/type/{type}")
    public ResponseEntity<List<User>> getPartnersByType(@PathVariable String type) {
        return ResponseEntity.ok(
                userService.getPartnersByType(
                        tn.esprit.pi_back.entities.enums.PartnerType.valueOf(type)
                )
        );
    }
}
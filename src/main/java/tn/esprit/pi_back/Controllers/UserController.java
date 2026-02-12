package tn.esprit.pi_back.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.Entities.User;
import tn.esprit.pi_back.Services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
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

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@Valid @PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
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
}
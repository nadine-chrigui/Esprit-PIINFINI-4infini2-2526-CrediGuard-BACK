package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.AuthRequest;
import tn.esprit.pi_back.dto.AuthResponse;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.security.JwtService;



    @RestController
    @RequestMapping("/api/auth")
    @RequiredArgsConstructor
    @CrossOrigin("*")
    public class AuthController {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;

        @PostMapping("/register")
        public User register(@Valid @RequestBody User user)
        {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        }

        @PostMapping("/login")
        public AuthResponse login(@RequestBody AuthRequest request) {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String token = jwtService.generateToken(request.getEmail());
            return new AuthResponse(token);
        }
    }


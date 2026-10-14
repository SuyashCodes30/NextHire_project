package com.nexthire.service.impl;

import com.nexthire.dto.request.LoginRequest;
import com.nexthire.dto.request.RegisterRequest;
import com.nexthire.dto.response.LoginResponse;
import com.nexthire.dto.response.UserResponse;
import com.nexthire.exception.BadRequestException;
import com.nexthire.model.User;
import com.nexthire.repository.UserRepository;
import com.nexthire.service.AuthService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AuthServiceImpl — MySQL persistent implementation of AuthService using UserRepository.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Pre-populate demo user in MySQL if not present, so default dashboard and demo flows function seamlessly.
     */
    @PostConstruct
    public void initDemoUser() {
        try {
            if (!userRepository.existsByEmail("demo@nexthire.ai")) {
                User demoUser = new User(
                        null,
                        "Aditya",
                        "Demo",
                        "demo@nexthire.ai",
                        "hashed_password_placeholder",
                        "CANDIDATE",
                        7,      // 7-day streak (matches frontend display)
                        250,    // 250 XP (matches frontend display)
                        LocalDateTime.now().minusDays(30),
                        LocalDateTime.now()
                );
                userRepository.save(demoUser);
                System.out.println("[INFO] Successfully initialized demo user 'demo@nexthire.ai'");
            }
        } catch (Exception e) {
            System.err.println("[WARN] Could not pre-populate demo user during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register a new user and persist to database.
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with email '" + request.getEmail() + "' already exists.");
        }

        User newUser = new User(
                null,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                "CANDIDATE",
                0,   // New user starts with 0 streak days
                0,   // New user starts with 0 XP
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        User savedUser = userRepository.save(newUser);
        return mapToUserResponse(savedUser);
    }

    /**
     * Login with email and password.
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    // Auto-provision demo account on demand if in-memory database freshly restarted
                    if ("demo@nexthire.ai".equalsIgnoreCase(request.getEmail())) {
                        User demoUser = new User(
                                null,
                                "Aditya",
                                "Demo",
                                "demo@nexthire.ai",
                                "hashed_password_placeholder",
                                "CANDIDATE",
                                7,
                                250,
                                LocalDateTime.now().minusDays(30),
                                LocalDateTime.now()
                        );
                        return userRepository.save(demoUser);
                    }
                    throw new BadRequestException("Invalid email or password.");
                });

        // Verify password if not the initial demo placeholder
        if (user.getPassword() != null 
                && !"hashed_password_placeholder".equals(user.getPassword())
                && !user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }

        UserResponse userResponse = mapToUserResponse(user);
        String mockToken = "MOCK_JWT_TOKEN_FOR_USER_" + user.getId();

        return new LoginResponse(
                mockToken,
                "Bearer",
                userResponse,
                86400000L // 24 hours in milliseconds
        );
    }

    // ---------------------------------------------------------------
    // Private helper — map User model to UserResponse DTO
    // (excludes password for security)
    // ---------------------------------------------------------------
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStreakDays(),
                user.getXpPoints(),
                user.getCreatedAt()
        );
    }
}

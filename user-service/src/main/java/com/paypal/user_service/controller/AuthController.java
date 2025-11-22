package com.paypal.user_service.controller;

import com.paypal.user_service.dto.JwtResponse;
import com.paypal.user_service.dto.LoginRequest;
import com.paypal.user_service.dto.SignupRequest;
import com.paypal.user_service.entity.User;
import com.paypal.user_service.repository.UserRepository;
import com.paypal.user_service.util.JWTUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request, 
            BindingResult bindingResult,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
             @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {
        logger.debug("Received signup request for email: {}", request.getEmail());
logger.info("Received request with TraceId: {}", traceId);
        // Validation error handling
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("error", errors));
        }

        try {
            // Check for existing user
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                logger.debug("Email already registered: {}", request.getEmail());
                return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
            }

            // Determine the role to assign
            String assignedRole = "USER"; // Default role
            String requestedRole = request.getRole();
            
            // If a specific role is requested, validate authorization
            if (requestedRole != null && !requestedRole.trim().isEmpty()) {
                requestedRole = requestedRole.trim().toUpperCase();
                
                // Check if requesting ADMIN role
                if ("ADMIN".equals(requestedRole)) {
                    // Verify that the requester has admin privileges
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        logger.warn("Attempt to create ADMIN user without authorization");
                        return ResponseEntity.status(401)
                            .body(Map.of("error", "Authorization required to create admin users"));
                    }
                    
                    try {
                        String token = authHeader.substring(7);
                        String requesterRole = jwtUtil.extractRole(token);
                        
                        if (!"ADMIN".equals(requesterRole)) {
                            logger.warn("Non-admin user attempted to create admin account. Requester role: {}", requesterRole);
                            return ResponseEntity.status(403)
                                .body(Map.of("error", "Only admin users can create admin accounts"));
                        }
                        
                        assignedRole = "ADMIN";
                        logger.info("Admin user creating new admin account for: {}", request.getEmail());
                        
                    } catch (Exception e) {
                        logger.error("Error validating admin token for signup", e);
                        return ResponseEntity.status(401)
                            .body(Map.of("error", "Invalid authorization token"));
                    }
                } else if ("USER".equals(requestedRole)) {
                    assignedRole = "USER";
                } else {
                    // Invalid role specified
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid role. Allowed roles: USER, ADMIN"));
                }
            }

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(assignedRole);

            User savedUser = userRepository.save(user);
            logger.debug("User registered successfully: {} with role: {}", savedUser.getEmail(), savedUser.getRole());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("email", savedUser.getEmail());
            response.put("role", savedUser.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during registration", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {
        logger.debug("Received login request for email: {}", request.getEmail());
logger.info("Received request with TraceId: {}", traceId);
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            logger.debug("User not found: {}", request.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.debug("Invalid password for user: {}", request.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        logger.debug("Login successful for user: {}", user.getEmail());

        return ResponseEntity.ok(new JwtResponse(token));
    }
}

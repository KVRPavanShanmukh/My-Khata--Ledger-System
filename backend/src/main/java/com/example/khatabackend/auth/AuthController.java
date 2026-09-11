package com.example.khatabackend.auth;

import com.example.khatabackend.user.User;
import com.example.khatabackend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://16.170.215.115:5173"}, allowCredentials = "true")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SignupVerificationService signupVerificationService;

    @Autowired
    private LoginVerificationService loginVerificationService;

    @PostMapping("/signup/request-code")
    public ResponseEntity<?> requestSignupCode(@RequestBody User user) {
        logger.info("Received signup request for mobile: {}", user.getMobile());

        try {
            signupVerificationService.requestCode(user);
            return ResponseEntity.ok(Map.of("message", "Verification code sent to your email"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email or mobile number already exists"));

        } catch (Exception e) {
            logger.error("Server error in signup: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error", "error", e.getMessage()));
        }
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verifySignup(@RequestBody Map<String, String> request) {
        try {
            User savedUser = signupVerificationService.verifyCode(request.get("email"), request.get("code"));
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email or mobile number already exists"));
        } catch (Exception e) {
            logger.error("Server error while verifying signup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error", "error", e.getMessage()));
        }
    }

    // Kept for the admin dashboard's customer-creation workflow.
    @PostMapping("/signup")
    public ResponseEntity<?> adminSignup(@RequestBody User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userService.saveUser(user);
            savedUser.setPassword(null);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email or mobile number already exists"));
        } catch (Exception e) {
            logger.error("Server error while creating admin customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error", "error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        logger.info("Received login request for mobile: {}", user.getMobile());

        try {
            if (user == null || user.getMobile() == null || user.getPassword() == null || user.getEmail() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mobile, email, and password required"));
            }

            Optional<User> optionalUser = userService.findByMobile(user.getMobile());

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "User not found"));
            }

            User existingUser = optionalUser.get();

            if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid mobile number or password"));
            }

            if (!existingUser.getEmail().equalsIgnoreCase(user.getEmail().trim())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Email does not match registered email"));
            }

            // Send OTP to email
            loginVerificationService.requestCode(existingUser);

            Map<String, String> response = new HashMap<>();
            response.put("mobile", existingUser.getMobile());
            response.put("message", "Phone and password verified, OTP sent to email");

            logger.info("Login pre-auth successful for mobile: {}, OTP sent", user.getMobile());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during login for mobile: {}", user.getMobile(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error", "error", e.getMessage()));
        }
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verifyLogin(@RequestBody Map<String, String> request) {
        try {
            User loggedInUser = loginVerificationService.verifyCode(request.get("mobile"), request.get("code"));
            loggedInUser.setPassword(null);
            return ResponseEntity.ok(loggedInUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Server error while verifying login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error", "error", e.getMessage()));
        }
    }

    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        logger.info("Received request to fetch all users");

        List<User> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null));

        return users;
    }

    @PutMapping("/users/{mobile}")
    public ResponseEntity<?> updateUser(@PathVariable String mobile, @RequestBody User updatedUser) {
        logger.info("Received request to update user with mobile: {}", mobile);

        try {
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            User savedUser = userService.updateUser(mobile, updatedUser);
            savedUser.setPassword(null);

            return ResponseEntity.ok(savedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update user", "error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{mobile}")
    public ResponseEntity<?> deleteUser(@PathVariable String mobile) {
        logger.info("Received request to delete user with mobile: {}", mobile);

        try {
            userService.deleteUserByMobile(mobile);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete user", "error", e.getMessage()));
        }
    }

    @GetMapping("/users/{mobile}")
    public ResponseEntity<?> getUserByMobile(@PathVariable String mobile) {
        logger.info("Received request to fetch user with mobile: {}", mobile);

        try {
            Optional<User> optionalUser = userService.findByMobile(mobile);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found with mobile: " + mobile));
            }

            User user = optionalUser.get();
            user.setPassword(null);

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch user", "error", e.getMessage()));
        }
    }
}
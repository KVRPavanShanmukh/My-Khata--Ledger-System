package com.example.khatabackend.auth;

import com.example.khatabackend.user.User;
import com.example.khatabackend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SignupVerificationService {
    private static final long CODE_LIFETIME_SECONDS = 10 * 60;

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final String fromAddress;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingSignup> pendingSignups = new ConcurrentHashMap<>();

    public SignupVerificationService(
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.fromAddress = fromAddress;
    }

    public void requestCode(User user) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException(
                    "SMTP is not configured. Set SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD in .env");
        }

        validateUser(user);
        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()
                || userRepository.findByMobile(user.getMobile()).isPresent()) {
            throw new IllegalArgumentException("Email or mobile number already exists");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        pendingSignups.put(email, new PendingSignup(user, code, Instant.now().plusSeconds(CODE_LIFETIME_SECONDS)));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("OmniBank signup verification code");
        message.setText("Your OmniBank verification code is " + code
                + ". It expires in 10 minutes. If you did not request this, ignore this email.");
        mailSender.send(message);
    }

    public User verifyCode(String email, String code) {
        if (email == null || code == null) {
            throw new IllegalArgumentException("Email and verification code are required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        PendingSignup pending = pendingSignups.get(normalizedEmail);
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            pendingSignups.remove(normalizedEmail);
            throw new IllegalArgumentException("Verification code is invalid or expired");
        }
        if (!pending.code().equals(code.trim())) {
            throw new IllegalArgumentException("Verification code is invalid or expired");
        }

        User user = pending.user();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        pendingSignups.remove(normalizedEmail);
        savedUser.setPassword(null);
        return savedUser;
    }

    private void validateUser(User user) {
        if (user == null || user.getEmail() == null || user.getPassword() == null
                || user.getMobile() == null || user.getName() == null
                || user.getGender() == null || user.getDob() == null) {
            throw new IllegalArgumentException("All signup fields are required");
        }
    }

    private record PendingSignup(User user, String code, Instant expiresAt) { }
}
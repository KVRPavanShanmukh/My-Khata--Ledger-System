package com.example.khatabackend.auth;

import com.example.khatabackend.user.User;
import com.example.khatabackend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginVerificationService {
    private static final long CODE_LIFETIME_SECONDS = 10 * 60;

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final String fromAddress;
    private final SecureRandom random = new SecureRandom();
    
    // Keyed by mobile number
    private final Map<String, PendingLogin> pendingLogins = new ConcurrentHashMap<>();

    public LoginVerificationService(
            JavaMailSender mailSender,
            UserRepository userRepository,
            @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.fromAddress = fromAddress;
    }

    public void requestCode(User user) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException(
                    "SMTP is not configured. Set SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD in .env");
        }

        String email = user.getEmail().trim().toLowerCase();
        String mobile = user.getMobile().trim();

        String code = String.format("%06d", random.nextInt(1_000_000));
        pendingLogins.put(mobile, new PendingLogin(user, code, Instant.now().plusSeconds(CODE_LIFETIME_SECONDS)));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("OmniBank Login OTP");
        message.setText("Your OmniBank login OTP is " + code
                + ". It expires in 10 minutes. If you did not attempt to login, ignore this email.");
        mailSender.send(message);
    }

    public User verifyCode(String mobile, String code) {
        if (mobile == null || code == null) {
            throw new IllegalArgumentException("Mobile and verification code are required");
        }

        String normalizedMobile = mobile.trim();
        PendingLogin pending = pendingLogins.get(normalizedMobile);
        
        if (pending == null || pending.expiresAt().isBefore(Instant.now())) {
            pendingLogins.remove(normalizedMobile);
            throw new IllegalArgumentException("OTP is invalid or expired");
        }
        
        if (!pending.code().equals(code.trim())) {
            throw new IllegalArgumentException("OTP is invalid or expired");
        }

        User user = pending.user();
        pendingLogins.remove(normalizedMobile);
        return user;
    }

    private record PendingLogin(User user, String code, Instant expiresAt) { }
}

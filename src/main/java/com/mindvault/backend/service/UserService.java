package com.mindvault.backend.service;

import com.mindvault.backend.config.JwtUtils;
import com.mindvault.backend.dto.AuthenticationDTO;
import com.mindvault.backend.model.User;
import com.mindvault.backend.model.UserSession;
import com.mindvault.backend.model.VerificationToken;
import com.mindvault.backend.repository.UserRepository;
import com.mindvault.backend.repository.UserSessionRepository;
import com.mindvault.backend.repository.VerificationTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.URI;
import java.time.LocalDateTime;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserSessionRepository userSessionRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, JwtUtils jwtUtils, UserSessionRepository userSessionRepository, VerificationTokenRepository verificationTokenRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.userSessionRepository = userSessionRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticateUser(AuthenticationDTO authenticationDTO) {
        User user = userRepository.findByEmailUser(authenticationDTO.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            sendVerificationEmail(user);
            throw new RuntimeException("Please verify your email. A verification email has been sent to your account.");
        }

        if (!passwordEncoder.matches(authenticationDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password.");
        }

        String username = user.getFirstName();
        String token = generateUserSession(user, username);

        logger.info("User information : {}", username);

        return token;
    }

    private String generateUserSession(User user, String username) {
        String token = jwtUtils.generateToken(user.getEmail(), username);
        UserSession userSession = new UserSession(user, token, LocalDateTime.now().plusHours(1));
        userSessionRepository.save(userSession);
        return token;
    }

    private void sendVerificationEmail(User user) {
        sendEmailVerification(user);
    }

    public void sendEmailVerification(User user) {
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified.");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getFirstName());
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(15);

        VerificationToken verificationToken = new VerificationToken(token, user, expirationDate);
        verificationTokenRepository.save(verificationToken);

        String verificationLink = "https://mindvault/auth/verify-email?token=" + token;

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<p>Welcome to MindVault app !</p>");
        emailBody.append("<p>Clique on the link below to validate your account creation :</p>");
        emailBody.append("<a href='").append(verificationLink).append("'>Validate my account</a>");

        emailService.sendEmail(
                user.getEmail(),
                "MindVault account verification link",
                emailBody.toString()
        );
    }

    public ResponseEntity<?> verifyEmail(String token) {
        String email = jwtUtils.getEmailFromToken(token);
        User user = userRepository.findByEmailUser(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (jwtUtils.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://mindvault/..."))
                    .build();
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://mindvault/..."))
                .build();
    }

}

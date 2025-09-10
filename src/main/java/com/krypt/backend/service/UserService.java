package com.krypt.backend.service;

import com.krypt.backend.config.JwtUtils;
import com.krypt.backend.dto.UserDTO.AuthenticationDTO;
import com.krypt.backend.dto.UserDTO.PasswordChangeDTO;
import com.krypt.backend.dto.UserDTO.RegisterDTO;
import com.krypt.backend.model.Token;
import com.krypt.backend.model.User;
import com.krypt.backend.model.enums.TokenType;
import com.krypt.backend.repository.TokenRepository;
import com.krypt.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager, TokenRepository tokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    private String buildVerificationEmail(User user, String verificationLink) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "  body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                "  .container { background-color: #fff; border-radius: 8px; padding: 30px; max-width: 600px; margin: auto; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                "  h2 { color: #171717; }" +
                "  p { font-size: 16px; color: #333; line-height: 1.5; }" +
                "  .button { display: inline-block; padding: 12px 24px; margin-top: 20px;" +
                "            background: linear-gradient(to right, #5554dc, #00b0cf);" +
                "            color: #fff; text-decoration: none; border-radius: 6px; font-weight: bold;" +
                "            transition: opacity 0.3s; }" +
                "  .button:hover { opacity: 0.9; }" +
                "  .footer { margin-top: 40px; font-size: 13px; color: #777; text-align: center; }" +
                "  .footer img { max-width: 65px; margin-top: 10px; border-radius: 50%; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h2>Welcome " + user.getFirstName() + "!</h2>" +
                "<p>Thank you for signing up for <strong>Krypt</strong>.</p>" +
                "<p>To activate your account, please click the button below:</p>" +
                "<a class='button' href='" + verificationLink + "'>Activate My Account</a>" +
                "<p style='margin-top: 30px;'>If you did not create an account, you can safely ignore this email.</p>" +
                "<div class='footer'>" +
                "<p>The Krypt Team</p>" +
                "<img src='https://i.imgur.com/bSRLqMp.png' alt='Logo' />" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public void register(RegisterDTO registerDTO) {
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        userRepository.save(user);

        String tokenValue = UUID.randomUUID().toString();
        Token token = new Token();
        token.setToken(tokenValue);
        token.setTokenType(TokenType.VERIFY_EMAIL);
        token.setExpired(false);
        token.setRevoked(false);
        token.setUser(user);
        tokenRepository.save(token);

        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + tokenValue;
        emailService.sendEmail(user.getEmail(), "Verify Your Krypt Account", buildVerificationEmail(user, verificationLink));
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("User already verified");
        }

        String tokenValue = UUID.randomUUID().toString();
        Token token = new Token();
        token.setToken(tokenValue);
        token.setTokenType(TokenType.VERIFY_EMAIL);
        token.setExpired(false);
        token.setRevoked(false);
        token.setUser(user);
        tokenRepository.save(token);

        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + tokenValue;
        emailService.sendEmail(user.getEmail(), "Verify Your Krypt Account", buildVerificationEmail(user, verificationLink));
    }

    public String authenticateUser(AuthenticationDTO authenticationDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationDTO.getEmail(), authenticationDTO.getPassword())
        );

        String jwt = jwtUtils.generateToken(
                authentication.getName(),
                "notes:read", "notes:write", "notes:tags"
        );

        User user = userRepository.findByEmail(authenticationDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Token> oldTokens = tokenRepository.findAllByUser(user);
        oldTokens.stream().filter(t -> t.isExpired() || t.isRevoked()).forEach(tokenRepository::delete);

        Token token = new Token();
        token.setToken(jwt);
        token.setUser(user);
        token.setTokenType(TokenType.ACCESS);
        token.setExpired(false);
        token.setRevoked(false);
        tokenRepository.save(token);

        return jwt;
    }

    public void changePassword(String email, PasswordChangeDTO passwordChangeDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);
    }
}

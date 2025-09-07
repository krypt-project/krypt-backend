package com.mindvault.backend.controller;

import com.mindvault.backend.config.JwtUtils;
import com.mindvault.backend.dto.UserDTO.AuthenticationDTO;
import com.mindvault.backend.dto.UserDTO.PasswordChangeDTO;
import com.mindvault.backend.dto.UserDTO.RegisterDTO;
import com.mindvault.backend.model.Token;
import com.mindvault.backend.model.User;
import com.mindvault.backend.repository.TokenRepository;
import com.mindvault.backend.repository.UserRepository;
import com.mindvault.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("http://mindvault")
public class AuthenticationController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthenticationController(UserService userService, UserRepository userRepository, TokenRepository tokenRepository, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String tokenValue) {
        Token token = tokenRepository.findByToken(tokenValue).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalide"));

        if (token.isExpired() || token.isRevoked()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired");
        }

        User user = token.getUser();
        if (user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This account is already verified");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(token);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000/account-verified"))
                .build();
    }

    @GetMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam("email") String email) {
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok("Verification link resend");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationDTO authenticationDTO) {
        String email = authenticationDTO.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!user.isEmailVerified()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "This account is not verified",
                            "message", "Please verify your email before login to MindVault",
                            "resendVerificationLink", "/api/auth/resend-verification?email=" + email
                    ));
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, authenticationDTO.getPassword())
        );

        String jwt = userService.authenticateUser(authenticationDTO);
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }
        String tokenValue = authHeader.substring(7);
        tokenRepository.findByToken(tokenValue).ifPresent(tokenRepository::delete);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal String email, @RequestBody PasswordChangeDTO passwordChangeDto) {
        userService.changePassword(email, passwordChangeDto);
        return ResponseEntity.ok("Password changed successfully");
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String tokenValue = authHeader.substring(7);
        Token token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalide"));

        if (token.isExpired() || token.isRevoked()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired or revoked");
        }

        return ResponseEntity.ok().build();
    }
}

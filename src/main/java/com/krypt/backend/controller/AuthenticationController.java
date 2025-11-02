package com.krypt.backend.controller;

import com.krypt.backend.config.JwtUtils;
import com.krypt.backend.dto.UserDTO.AuthenticationDTO;
import com.krypt.backend.dto.UserDTO.PasswordChangeDTO;
import com.krypt.backend.dto.UserDTO.RegisterDTO;
import com.krypt.backend.dto.UserDTO.VerificationResponseDTO;
import com.krypt.backend.model.Token;
import com.krypt.backend.model.User;
import com.krypt.backend.repository.TokenRepository;
import com.krypt.backend.repository.UserRepository;
import com.krypt.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashMap;
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
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(
            @RequestParam("token") String tokenValue,
            @RequestHeader(value = "Accept", defaultValue = "application/json") String acceptHeader) {

        Token token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalide"));

        User user = token.getUser();

        if (token.isExpired() || token.isRevoked()) {
            if (acceptHeader.contains("text/html")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("<h1>Token expired</h1>");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new VerificationResponseDTO(false));
            }
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
            tokenRepository.delete(token);
        }

        if (acceptHeader.contains("text/html")) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:3000/account-verified"))
                    .build();
        } else {
            return ResponseEntity.ok(new VerificationResponseDTO(true));
        }
    }

    @GetMapping("/is-verified")
    public ResponseEntity<VerificationResponseDTO> isVerified(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(new VerificationResponseDTO(user.isEmailVerified()));
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

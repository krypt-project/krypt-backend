package com.mindvault.backend.controller;

import com.mindvault.backend.config.JwtUtils;
import com.mindvault.backend.dto.AuthenticationDTO;
import com.mindvault.backend.service.UserService;
import com.mindvault.backend.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/authentication")
@CrossOrigin("https://mindvault")
public class AuthenticationController {
    private final UserService userService;
    private final UserSessionService userSessionService;
    private final JwtUtils jwtUtils;

    public AuthenticationController(UserService userService, UserSessionService userSessionService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.userSessionService = userSessionService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/login")
    public ResponseEntity<?> Login(@RequestBody AuthenticationDTO authenticationDTO) {
        try {
            String token = userService.authenticateUser(authenticationDTO);
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Please verify your credentials")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("A verification link as been sent to you. Please verify your account");
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> Logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            userSessionService.invalidateUserSession(token);
            return ResponseEntity.ok("Logout successful");
        }

        return ResponseEntity.badRequest().body("Invalid Token");
    }
}

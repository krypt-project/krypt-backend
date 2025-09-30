package com.krypt.backend.controller;

import com.krypt.backend.config.JwtUtils;
import com.krypt.backend.dto.UserDTO.PatchUserDTO;
import com.krypt.backend.dto.UserDTO.UserDTO;
import com.krypt.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);

        String email;
        try {
            email = jwtUtils.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

        return userService.getUserInfoByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@Valid @RequestBody PatchUserDTO patchUserDTO, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
            String email;
        try{
            email = jwtUtils.extractUsername(token);
        }catch(Exception e){
            return ResponseEntity.status(401).build();
        }
        return userService.PatchUserInfoByEmail(email, patchUserDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

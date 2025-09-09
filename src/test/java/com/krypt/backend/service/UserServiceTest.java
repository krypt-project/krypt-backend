package com.krypt.backend.service;

import com.krypt.backend.config.JwtUtils;
import com.krypt.backend.dto.UserDTO.AuthenticationDTO;
import com.krypt.backend.dto.UserDTO.PasswordChangeDTO;
import com.krypt.backend.dto.UserDTO.RegisterDTO;
import com.krypt.backend.model.Token;
import com.krypt.backend.model.User;
import com.krypt.backend.repository.TokenRepository;
import com.krypt.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private RegisterDTO registerDTO;

    @BeforeEach
    void setup() {
        registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@example.com");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("TEST");
        registerDTO.setPassword("password");
    }

    // ------------------- CREATE -------------------
    @Test
    void registerShouldSaveUserAndSendEmail() {
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        userService.register(registerDTO);

        verify(userRepository).save(any(User.class));
        verify(tokenRepository).save(any(Token.class));
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void registerShouldThrowIfEmailExists() {
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(registerDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    // ------------------- UPDATE -------------------
    @Test
    void changePasswordShouldChangePassword() {
        User user = new User();
        user.setPassword("password");

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("oldPassword");
        dto.setNewPassword("newPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword("test@example.com", dto);

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void changePasswordShouldThrowIfOldPasswordWrong() {
        User user = new User();
        user.setPassword("encodedOldPassword");

        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("wrongOldPassword");
        dto.setNewPassword("newPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("test@example.com", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Old password does not match");

        verify(userRepository, never()).save(any());
    }

    // ------------------- READ -------------------
    @Test
    void authenticateUserShouldAuthenticateUserAndSaveToken() {
        AuthenticationDTO dto = new AuthenticationDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        User user = new User();
        user.setEmail("test@example.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateToken(anyString(), any(), any(), any())).thenReturn("jwt-token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findAllByUser(user)).thenReturn(List.of());

        String jwt = userService.authenticateUser(dto);

        assertThat(jwt).isEqualTo("jwt-token");
        verify(tokenRepository).save(any());
    }
}

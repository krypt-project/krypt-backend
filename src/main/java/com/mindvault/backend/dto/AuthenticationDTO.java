package com.mindvault.backend.dto;

public class AuthenticationDTO {
    private String email;
    private String password;
    private String token;

    // Getter
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }
}

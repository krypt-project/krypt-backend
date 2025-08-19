package com.mindvault.backend.dto.UserDTO;

public class AuthenticationDTO {
    private String email;
    private String password;
    private String token;

    public AuthenticationDTO() {}

    public AuthenticationDTO(String email, String password, String token) {
        this.email = email;
        this.password = password;
        this.token = token;
    }

    // Getter & Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

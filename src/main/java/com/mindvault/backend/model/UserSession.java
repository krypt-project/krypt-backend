package com.mindvault.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "userSession")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userSession_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expirationData;

    // Constructor

    public UserSession() {}

    public UserSession(User user, String token, LocalDateTime expirationData) {
        this.user = user;
        this.token = token;
        this.expirationData = expirationData;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationData);
    }
}

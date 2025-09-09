package com.krypt.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_table")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private String creationDate;

    @Column(updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private String modificationDate;

    @Column(nullable = false)
    private boolean emailVerified = false;

    // Date
    @PrePersist
    protected void onCreate() {
        creationDate = LocalDateTime.now().toString();
        modificationDate = LocalDateTime.now().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        modificationDate = LocalDateTime.now().toString();
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

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

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}

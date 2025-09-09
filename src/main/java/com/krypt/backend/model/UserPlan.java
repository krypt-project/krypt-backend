package com.krypt.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "userplan_table")
public class UserPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userplan_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role baseRole;

    @Column(nullable = false)
    private Integer currentStorageGb;

    @Column(nullable = false)
    private Integer currentAiQuota;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentPricePerMonth;

    @PrePersist
    protected void onCreate() {
        if (baseRole != null) {
            this.currentStorageGb = baseRole.getMaxStorageGb();
            this.currentAiQuota = baseRole.getAiQuota();
            this.currentPricePerMonth = baseRole.getPricePerMonth();
        }
    }

    // Constructor
    public UserPlan() {}

    public UserPlan(Long id, User user, Role baseRole, Integer currentStorageGb, Integer currentAiQuota, BigDecimal currentPricePerMonth) {
        this.id = id;
        this.user = user;
        this.baseRole = baseRole;
        this.currentStorageGb = currentStorageGb;
        this.currentAiQuota = currentAiQuota;
        this.currentPricePerMonth = currentPricePerMonth;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getBaseRole() {
        return baseRole;
    }

    public void setBaseRole(Role baseRole) {
        this.baseRole = baseRole;
    }

    public Integer getCurrentStorageGb() {
        return currentStorageGb;
    }

    public void setCurrentStorageGb(Integer currentStorageGb) {
        this.currentStorageGb = currentStorageGb;
    }

    public Integer getCurrentAiQuota() {
        return currentAiQuota;
    }

    public void setCurrentAiQuota(Integer currentAiQuota) {
        this.currentAiQuota = currentAiQuota;
    }

    public BigDecimal getCurrentPricePerMonth() {
        return currentPricePerMonth;
    }

    public void setCurrentPricePerMonth(BigDecimal currentPricePerMonth) {
        this.currentPricePerMonth = currentPricePerMonth;
    }
}

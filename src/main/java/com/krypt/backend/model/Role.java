package com.krypt.backend.model;

import com.krypt.backend.model.enums.RoleType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "role_table")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleType roleType;

    @Column(nullable = false)
    private Integer maxStorageGb;

    @Column(nullable = false)
    private Integer aiQuota;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerMonth;

    @Column(length = 500)
    private String description;

    // Constructor
    public Role() {}

    public Role(RoleType roleType, Integer maxStorageGb, Integer aiQuota, BigDecimal pricePerMonth, String description) {
        this.roleType = roleType;
        this.maxStorageGb = maxStorageGb;
        this.aiQuota = aiQuota;
        this.pricePerMonth = pricePerMonth;
        this.description = description;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public Integer getMaxStorageGb() {
        return maxStorageGb;
    }

    public void setMaxStorageGb(Integer maxStorageGb) {
        this.maxStorageGb = maxStorageGb;
    }

    public Integer getAiQuota() {
        return aiQuota;
    }

    public void setAiQuota(Integer aiQuota) {
        this.aiQuota = aiQuota;
    }

    public BigDecimal getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(BigDecimal pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
package com.sunfeax.citeria.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "businesses")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessEntity {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity owner;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(min = 2, max = 120)
    private String name;

    @Column(name = "description")
    @Size(max = 1000)
    private String description;

    @Column(name = "phone", length = 20)
    @Size(min = 7, max = 20)
    private String phone;

    @Column(name = "email", length = 100)
    @Email
    @Size(max = 100)
    private String email;

    @Column(name = "website", length = 255)
    @Size(max = 255)
    private String website;

    @Column(name = "address", length = 255)
    @Size(max = 255)
    private String address;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "business")
    private List<ServiceEntity> services;

    @OneToMany(mappedBy = "business")
    private List<OfferingEntity> offerings;
}

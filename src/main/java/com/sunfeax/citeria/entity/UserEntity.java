package com.sunfeax.citeria.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "type", nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private UserType type = UserType.CLIENT;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner")
    private List<BusinessEntity> ownedBusinesses;

    @OneToMany(mappedBy = "specialist")
    private List<OfferingEntity> offerings;

    @OneToMany(mappedBy = "client")
    private List<AppointmentEntity> clientAppointments;
}

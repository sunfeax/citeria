package com.sunfeax.citeria.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "services")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private BusinessEntity business;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Service name cannot be empty")
    @Size(min = 3, max = 120)
    private String name;

    @Column(name = "description")
    @Size(max = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    @NotNull
    @Min(15)
    @Max(480)
    private Integer durationMinutes;

    @Column(name = "price_amount", nullable = false, precision = 12, scale = 2)
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal priceAmount;

    @Column(name = "currency", nullable = false, length = 3)
    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "service")
    private List<OfferingEntity> offerings;
}

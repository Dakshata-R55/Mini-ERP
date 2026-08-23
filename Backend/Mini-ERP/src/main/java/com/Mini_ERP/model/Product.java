package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductType productType = ProductType.FINISHED_GOOD;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salesPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal onHandQty = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal reservedQty = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProcurementStrategy procurementStrategy = ProcurementStrategy.MTS;

  /** MTO flag from wireframe ("Procure on Demand") */
    @Column(nullable = false)
    @Builder.Default
    private boolean procureOnDemand = false;

    @Enumerated(EnumType.STRING)
    private ProcurementType procurementType;

    /** Placeholder until Vendor module exists */
    private Long vendorId;
    private String vendorName;

    /** Placeholder until BoM module exists */
    private Long bomId;
    private String bomName;

    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (onHandQty == null) {
            onHandQty = BigDecimal.ZERO;
        }
        if (reservedQty == null) {
            reservedQty = BigDecimal.ZERO;
        }
        syncProcurementStrategy();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
        syncProcurementStrategy();
    }

    private void syncProcurementStrategy() {
        procurementStrategy = procureOnDemand
                ? ProcurementStrategy.MTO
                : ProcurementStrategy.MTS;
    }

    /** Free to Use = On Hand - Reserved (PDF formula) */
    public BigDecimal getFreeToUseQty() {
        return onHandQty.subtract(reservedQty);
    }
}
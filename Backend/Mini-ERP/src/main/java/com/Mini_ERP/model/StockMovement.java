package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityDelta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErpModule sourceModule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType movementType;

    private String documentReference;

    private Long documentId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private String createdBy;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

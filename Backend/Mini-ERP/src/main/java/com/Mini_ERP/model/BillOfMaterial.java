package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bill_of_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillOfMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finished_product_id", nullable = false)
    private Product finishedProduct;

    /** Finished units produced per BOM batch */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal outputQty = BigDecimal.ONE;

    @OneToMany(mappedBy = "billOfMaterial", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BomComponentLine> components = new ArrayList<>();

    @OneToMany(mappedBy = "billOfMaterial", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BomOperation> operations = new ArrayList<>();

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
        if (outputQty == null) {
            outputQty = BigDecimal.ONE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

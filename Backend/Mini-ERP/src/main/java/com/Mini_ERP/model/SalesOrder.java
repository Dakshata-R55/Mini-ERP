package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SalesOrderStatus status = SalesOrderStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String customerAddress;

    @Column(nullable = false, updatable = false)
    private Instant creationDate;

    @Column(nullable = false)
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id")
    private AppUser salesPerson;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderLine> lines = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (creationDate == null) {
            creationDate = Instant.now();
        }
        if (status == null) {
            status = SalesOrderStatus.DRAFT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
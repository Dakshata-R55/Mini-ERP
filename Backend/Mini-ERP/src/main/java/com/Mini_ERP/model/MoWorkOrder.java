package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mo_work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoWorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrder manufacturingOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_center_id", nullable = false)
    private WorkCenter workCenter;

    @Column(nullable = false)
    @Builder.Default
    private int sequence = 1;

    @Column(nullable = false)
    @Builder.Default
    private long expectedDurationMinutes = 0;

    @Column(nullable = false)
    @Builder.Default
    private long realDurationMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MoWorkOrderStatus status = MoWorkOrderStatus.PENDING;
}

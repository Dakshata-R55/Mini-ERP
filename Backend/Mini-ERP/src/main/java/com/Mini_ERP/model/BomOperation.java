package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bom_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterial billOfMaterial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_center_id", nullable = false)
    private WorkCenter workCenter;

    @Column(nullable = false)
    @Builder.Default
    private int sequence = 1;

    /** Expected duration in minutes */
    @Column(nullable = false)
    @Builder.Default
    private long expectedDurationMinutes = 0;
}

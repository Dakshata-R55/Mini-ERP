package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "mo_component_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoComponentLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrder manufacturingOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal toConsumeQty;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal consumedQty = BigDecimal.ZERO;
}

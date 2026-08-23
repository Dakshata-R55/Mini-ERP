package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bom_component_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomComponentLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterial billOfMaterial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_product_id", nullable = false)
    private Product componentProduct;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal qtyPerOutput;
}

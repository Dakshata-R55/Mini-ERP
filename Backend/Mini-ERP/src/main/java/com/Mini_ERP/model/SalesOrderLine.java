package com.Mini_ERP.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal orderedQty;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deliveredQty = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    private String units;

    /** True when orderedQty > freeToUse at confirm time (shortage / MTO). */
    @Column(nullable = false)
    @Builder.Default
    private boolean availabilityShortage = false;

    public BigDecimal getLineTotal() {
        BigDecimal qty = deliveredQty.compareTo(BigDecimal.ZERO) > 0 ? deliveredQty : orderedQty;
        return qty.multiply(unitPrice);
    }

    public BigDecimal getPendingQty() {
        return orderedQty.subtract(deliveredQty);
    }
}
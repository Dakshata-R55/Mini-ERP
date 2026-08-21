package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalesOrderLineResponse {

    private Long id;
    private Long productId;
    private String productReference;
    private String productName;
    private BigDecimal orderedQty;
    private BigDecimal deliveredQty;
    private BigDecimal unitPrice;
    private String units;
    private boolean availabilityShortage;
    private BigDecimal lineTotal;
    private BigDecimal freeToUseQty;
}
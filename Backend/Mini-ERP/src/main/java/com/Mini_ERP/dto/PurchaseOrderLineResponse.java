package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PurchaseOrderLineResponse {

    private Long id;
    private Long productId;
    private String productReference;
    private String productName;
    private BigDecimal orderedQty;
    private BigDecimal receivedQty;
    private BigDecimal unitCostPrice;
    private String units;
    private BigDecimal lineTotal;
}
package com.Mini_ERP.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderLineRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal orderedQty;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitCostPrice;

    private String units;
}
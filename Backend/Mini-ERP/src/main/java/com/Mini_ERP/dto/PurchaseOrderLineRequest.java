package com.Mini_ERP.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderLineRequest {

    /** Existing raw material — optional when productName is provided. */
    private Long productId;

    @Size(max = 200)
    private String productName;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal orderedQty;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitCostPrice;

    private String units;
}

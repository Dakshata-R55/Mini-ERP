package com.Mini_ERP.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderLineRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal orderedQty;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitPrice;

    private String units;
}
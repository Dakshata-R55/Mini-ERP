package com.Mini_ERP.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManufacturingOrderRequest {

    @NotNull
    private Long finishedProductId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal qtyToProduce;

    private Long bomId;

    private Long assigneeId;

    private Long salesOrderId;
}

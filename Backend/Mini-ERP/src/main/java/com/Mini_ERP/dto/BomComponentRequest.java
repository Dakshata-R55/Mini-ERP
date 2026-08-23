package com.Mini_ERP.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BomComponentRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal qtyPerOutput;
}

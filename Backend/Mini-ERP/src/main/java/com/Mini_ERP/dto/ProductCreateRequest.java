package com.Mini_ERP.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCreateRequest extends ProductRequest {

    /** Optional. Only Project Manager may set > 0 — becomes On Hand (inventory). */
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal openingStock;
}
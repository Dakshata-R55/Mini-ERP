package com.Mini_ERP.dto;

import com.Mini_ERP.model.ProductType;
import com.Mini_ERP.model.ProcurementType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotNull
    private ProductType productType;

  /** Required for finished goods; raw materials may use zero. */
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal salesPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costPrice;

    private boolean procureOnDemand;

    private ProcurementType procurementType;

    private Long vendorId;
    private String vendorName;

    private Long bomId;
    private String bomName;

    private String imageUrl;
}
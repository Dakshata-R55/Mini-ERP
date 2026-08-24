package com.Mini_ERP.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BomRequest {

    /** Existing finished good — optional when finishedProductName is provided. */
    private Long finishedProductId;

    @Size(max = 200)
    private String finishedProductName;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal outputQty;

    @Valid
    private List<BomComponentRequest> components = new ArrayList<>();

    @Valid
    private List<BomOperationRequest> operations = new ArrayList<>();
}

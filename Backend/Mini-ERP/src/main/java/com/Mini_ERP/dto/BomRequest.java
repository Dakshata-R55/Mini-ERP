package com.Mini_ERP.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BomRequest {

    @NotNull
    private Long finishedProductId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal outputQty;

    @Valid
    private List<BomComponentRequest> components = new ArrayList<>();

    @Valid
    private List<BomOperationRequest> operations = new ArrayList<>();
}

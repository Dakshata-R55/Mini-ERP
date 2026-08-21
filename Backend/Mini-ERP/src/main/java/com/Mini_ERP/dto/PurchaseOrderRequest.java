package com.Mini_ERP.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull
    private Long vendorId;

    @Size(max = 500)
    private String vendorAddress;

    @NotNull
    private LocalDate startDate;

    private Long responsiblePersonId;

    @NotEmpty
    @Valid
    private List<PurchaseOrderLineRequest> lines;
}
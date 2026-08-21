package com.Mini_ERP.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SalesOrderRequest {

    @NotNull
    private Long customerId;

    @Size(max = 500)
    private String customerAddress;

    @NotNull
    private LocalDate startDate;

    private Long salesPersonId;

    @NotEmpty
    @Valid
    private List<SalesOrderLineRequest> lines;
}
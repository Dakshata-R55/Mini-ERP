package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BomResponse {

    private Long id;
    private String reference;
    private Long finishedProductId;
    private String finishedProductName;
    private BigDecimal outputQty;
    private List<BomComponentResponse> components;
    private List<BomOperationResponse> operations;
}

package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductListResponse {

    private Long id;
    private String reference;
    private String name;
    private BigDecimal salesPrice;
    private BigDecimal costPrice;
    private BigDecimal onHandQty;
    private BigDecimal freeToUseQty;
    private boolean procureOnDemand;
}
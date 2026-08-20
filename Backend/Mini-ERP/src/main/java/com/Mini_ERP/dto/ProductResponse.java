package com.Mini_ERP.dto;

import com.Mini_ERP.model.ProcurementStrategy;
import com.Mini_ERP.model.ProcurementType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private String reference;
    private String name;
    private BigDecimal salesPrice;
    private BigDecimal costPrice;
    private BigDecimal onHandQty;
    private BigDecimal reservedQty;
    private BigDecimal freeToUseQty;
    private ProcurementStrategy procurementStrategy;
    private boolean procureOnDemand;
    private ProcurementType procurementType;
    private Long vendorId;
    private String vendorName;
    private Long bomId;
    private String bomName;
    private String imageUrl;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
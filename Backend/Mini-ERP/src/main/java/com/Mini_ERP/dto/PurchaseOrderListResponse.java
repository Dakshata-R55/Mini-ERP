package com.Mini_ERP.dto;

import com.Mini_ERP.model.PurchaseOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PurchaseOrderListResponse {

    private Long id;
    private String reference;
    private LocalDate startDate;
    private String vendorName;
    private String responsiblePersonName;
    private PurchaseOrderStatus status;
    private BigDecimal orderTotal;
}
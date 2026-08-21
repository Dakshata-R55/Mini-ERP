package com.Mini_ERP.dto;

import com.Mini_ERP.model.PurchaseOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PurchaseOrderResponse {

    private Long id;
    private String reference;
    private PurchaseOrderStatus status;
    private Long vendorId;
    private String vendorName;
    private String vendorAddress;
    private Instant creationDate;
    private LocalDate startDate;
    private Long responsiblePersonId;
    private String responsiblePersonName;
    private BigDecimal orderTotal;
    private List<PurchaseOrderLineResponse> lines;
}
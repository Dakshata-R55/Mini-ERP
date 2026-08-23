package com.Mini_ERP.dto;

import com.Mini_ERP.model.ManufacturingOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ManufacturingOrderListResponse {

    private Long id;
    private String reference;
    private ManufacturingOrderStatus status;
    private String finishedProductName;
    private BigDecimal qtyToProduce;
    private String assigneeName;
}

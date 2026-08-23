package com.Mini_ERP.dto;

import com.Mini_ERP.model.ManufacturingOrderStatus;
import com.Mini_ERP.model.MoWorkOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ManufacturingOrderResponse {

    private Long id;
    private String reference;
    private ManufacturingOrderStatus status;
    private Long finishedProductId;
    private String finishedProductName;
    private BigDecimal qtyToProduce;
    private Long bomId;
    private String bomReference;
    private Long assigneeId;
    private String assigneeName;
    private Long salesOrderId;
    private BigDecimal totalProductionCost;
    private List<MoComponentResponse> components;
    private List<MoWorkOrderResponse> workOrders;
}

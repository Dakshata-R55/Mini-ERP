package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {

    private OrderModuleDashboardResponse salesOrders;
    private OrderModuleDashboardResponse purchaseOrders;
    private OrderModuleDashboardResponse manufacturingOrders;
}
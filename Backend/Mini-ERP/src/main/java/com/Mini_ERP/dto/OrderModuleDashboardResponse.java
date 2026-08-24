package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderModuleDashboardResponse {

    private OrderStatusCountsResponse all;
    private OrderStatusCountsResponse mine;
}
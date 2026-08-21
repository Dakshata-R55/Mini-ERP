package com.Mini_ERP.dto;

import com.Mini_ERP.model.SalesOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalesOrderListResponse {

    private Long id;
    private String reference;
    private LocalDate startDate;
    private String customerName;
    private String salesPersonName;
    private SalesOrderStatus status;
    private BigDecimal orderTotal;
}
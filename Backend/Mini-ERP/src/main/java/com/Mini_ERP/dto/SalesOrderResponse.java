package com.Mini_ERP.dto;

import com.Mini_ERP.model.SalesOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalesOrderResponse {

    private Long id;
    private String reference;
    private SalesOrderStatus status;
    private Long customerId;
    private String customerName;
    private String customerAddress;
    private Instant creationDate;
    private LocalDate startDate;
    private Long salesPersonId;
    private String salesPersonName;
    private BigDecimal orderTotal;
    private List<SalesOrderLineResponse> lines;
}
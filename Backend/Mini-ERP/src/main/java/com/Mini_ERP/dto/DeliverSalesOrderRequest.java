package com.Mini_ERP.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliverSalesOrderRequest {

    @NotEmpty
    @Valid
    private List<LineDelivery> lines;

    @Data
    public static class LineDelivery {

        @NotNull
        private Long lineId;

        /** Total delivered qty on this line (not increment). */
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal deliveredQty;
    }
}
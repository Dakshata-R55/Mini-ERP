package com.Mini_ERP.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BomOperationRequest {

    @NotNull
    private Long workCenterId;

    @Min(1)
    private int sequence = 1;

    @Min(0)
    private long expectedDurationMinutes = 0;
}

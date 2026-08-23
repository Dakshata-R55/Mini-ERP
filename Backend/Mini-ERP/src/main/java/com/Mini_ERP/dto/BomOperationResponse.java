package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BomOperationResponse {

    private Long id;
    private Long workCenterId;
    private String workCenterName;
    private String location;
    private int sequence;
    private long expectedDurationMinutes;
}

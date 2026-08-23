package com.Mini_ERP.dto;

import com.Mini_ERP.model.MoWorkOrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoWorkOrderResponse {

    private Long id;
    private Long workCenterId;
    private String workCenterName;
    private String location;
    private int sequence;
    private long expectedDurationMinutes;
    private long realDurationMinutes;
    private MoWorkOrderStatus status;
}

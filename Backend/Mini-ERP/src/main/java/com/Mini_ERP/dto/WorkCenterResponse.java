package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkCenterResponse {

    private Long id;
    private String name;
    private String location;
    private boolean active;
}

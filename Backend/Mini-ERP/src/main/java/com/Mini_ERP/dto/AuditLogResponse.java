package com.Mini_ERP.dto;

import com.Mini_ERP.model.AuditAction;
import com.Mini_ERP.model.ErpModule;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditLogResponse {

    private Long id;
    private ErpModule module;
    private Long entityId;
    private String entityReference;
    private AuditAction action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private Instant changedAt;
}
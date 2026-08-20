package com.Mini_ERP.controller;

import com.Mini_ERP.dto.AuditLogResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.AUDIT_LOGS, action = RequiresModuleAccess.Action.READ)
    public List<AuditLogResponse> getByModule(@RequestParam ErpModule module) {
        return auditLogService.getLogsByModule(module);
    }

    @GetMapping("/products/{productId}")
    @RequiresModuleAccess(module = ErpModule.PRODUCTS, action = RequiresModuleAccess.Action.READ)
    public List<AuditLogResponse> getProductLogs(@PathVariable Long productId) {
        return auditLogService.getLogsForEntity(ErpModule.PRODUCTS, productId);
    }
}
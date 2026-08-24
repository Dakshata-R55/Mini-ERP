package com.Mini_ERP.controller;

import com.Mini_ERP.dto.DashboardSummaryResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @RequiresModuleAccess(module = ErpModule.DASHBOARD, action = RequiresModuleAccess.Action.READ)
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }
}
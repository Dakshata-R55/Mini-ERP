package com.Mini_ERP.controller;

import com.Mini_ERP.dto.WorkCenterRequest;
import com.Mini_ERP.dto.WorkCenterResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.WorkCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-centers")
@RequiredArgsConstructor
public class WorkCenterController {

    private final WorkCenterService workCenterService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.READ)
    public List<WorkCenterResponse> listWorkCenters() {
        return workCenterService.listWorkCenters();
    }

    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.READ)
    public WorkCenterResponse getWorkCenter(@PathVariable Long id) {
        return workCenterService.getWorkCenter(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public WorkCenterResponse createWorkCenter(@Valid @RequestBody WorkCenterRequest request) {
        return workCenterService.createWorkCenter(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public WorkCenterResponse updateWorkCenter(
            @PathVariable Long id,
            @Valid @RequestBody WorkCenterRequest request) {
        return workCenterService.updateWorkCenter(id, request);
    }

    @DeleteMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public void deleteWorkCenter(@PathVariable Long id) {
        workCenterService.deleteWorkCenter(id);
    }
}

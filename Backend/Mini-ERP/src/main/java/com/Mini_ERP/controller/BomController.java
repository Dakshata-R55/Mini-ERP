package com.Mini_ERP.controller;

import com.Mini_ERP.dto.BomRequest;
import com.Mini_ERP.dto.BomResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.BomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.BOM, action = RequiresModuleAccess.Action.READ)
    public List<BomResponse> listBoms() {
        return bomService.listBoms();
    }

    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.BOM, action = RequiresModuleAccess.Action.READ)
    public BomResponse getBom(@PathVariable Long id) {
        return bomService.getBom(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.BOM, action = RequiresModuleAccess.Action.WRITE)
    public BomResponse createBom(@Valid @RequestBody BomRequest request) {
        return bomService.createBom(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.BOM, action = RequiresModuleAccess.Action.WRITE)
    public BomResponse updateBom(@PathVariable Long id, @Valid @RequestBody BomRequest request) {
        return bomService.updateBom(id, request);
    }

    @DeleteMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.BOM, action = RequiresModuleAccess.Action.WRITE)
    public void deleteBom(@PathVariable Long id) {
        bomService.deleteBom(id);
    }
}

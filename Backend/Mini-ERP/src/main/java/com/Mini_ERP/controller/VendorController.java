package com.Mini_ERP.controller;

import com.Mini_ERP.dto.VendorRequest;
import com.Mini_ERP.dto.VendorResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.READ)
    public List<VendorResponse> listVendors() {
        return vendorService.listVendors();
    }

    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.READ)
    public VendorResponse getVendor(@PathVariable Long id) {
        return vendorService.getVendor(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public VendorResponse createVendor(@Valid @RequestBody VendorRequest request) {
        return vendorService.createVendor(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public VendorResponse updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody VendorRequest request) {
        return vendorService.updateVendor(id, request);
    }

    @DeleteMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public void deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
    }
}
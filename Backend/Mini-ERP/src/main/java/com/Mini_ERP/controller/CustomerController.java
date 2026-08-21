package com.Mini_ERP.controller;

import com.Mini_ERP.dto.CustomerRequest;
import com.Mini_ERP.dto.CustomerResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.READ)
    public List<CustomerResponse> listCustomers() {
        return customerService.listCustomers();
    }

    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.READ)
    public CustomerResponse getCustomer(@PathVariable Long id) {
        return customerService.getCustomer(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.ADMIN)
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }
}
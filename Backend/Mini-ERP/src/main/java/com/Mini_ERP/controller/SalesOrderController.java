package com.Mini_ERP.controller;

import com.Mini_ERP.dto.*;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.READ)
    public List<SalesOrderListResponse> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean late,
            @RequestParam(required = false) String search) {
        return salesOrderService.listOrders(status, late, search);
    }

    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.READ)
    public SalesOrderResponse getOrder(@PathVariable Long id) {
        return salesOrderService.getOrder(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public SalesOrderResponse createOrder(@Valid @RequestBody SalesOrderRequest request) {
        return salesOrderService.createOrder(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public SalesOrderResponse updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody SalesOrderRequest request) {
        return salesOrderService.updateOrder(id, request);
    }

    @PostMapping("/{id}/confirm")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public SalesOrderResponse confirmOrder(@PathVariable Long id) {
        return salesOrderService.confirmOrder(id);
    }

    @PostMapping("/{id}/deliver")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public SalesOrderResponse deliverOrder(
            @PathVariable Long id,
            @Valid @RequestBody DeliverSalesOrderRequest request) {
        return salesOrderService.deliverOrder(id, request);
    }

    @PostMapping("/{id}/cancel")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public SalesOrderResponse cancelOrder(@PathVariable Long id) {
        return salesOrderService.cancelOrder(id);
    }

    @DeleteMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.SALES, action = RequiresModuleAccess.Action.WRITE)
    public void deleteOrder(@PathVariable Long id) {
        salesOrderService.deleteOrder(id);
    }
}
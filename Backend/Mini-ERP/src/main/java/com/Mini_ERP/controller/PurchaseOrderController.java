package com.Mini_ERP.controller;

import com.Mini_ERP.dto.*;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

@GetMapping
@RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.READ)
public List<PurchaseOrderListResponse> listOrders(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Boolean late,
        @RequestParam(required = false) Boolean mine,
        @RequestParam(required = false) String search) {
    return purchaseOrderService.listOrders(status, late, mine, search);
}
    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.READ)
    public PurchaseOrderResponse getOrder(@PathVariable Long id) {
        return purchaseOrderService.getOrder(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public PurchaseOrderResponse createOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.createOrder(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public PurchaseOrderResponse updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.updateOrder(id, request);
    }

    @PostMapping("/{id}/confirm")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public PurchaseOrderResponse confirmOrder(@PathVariable Long id) {
        return purchaseOrderService.confirmOrder(id);
    }

    @PostMapping("/{id}/receive")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public PurchaseOrderResponse receiveOrder(
            @PathVariable Long id,
            @Valid @RequestBody ReceivePurchaseOrderRequest request) {
        return purchaseOrderService.receiveOrder(id, request);
    }

    @PostMapping("/{id}/cancel")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public PurchaseOrderResponse cancelOrder(@PathVariable Long id) {
        return purchaseOrderService.cancelOrder(id);
    }

    @DeleteMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PURCHASE, action = RequiresModuleAccess.Action.WRITE)
    public void deleteOrder(@PathVariable Long id) {
        purchaseOrderService.deleteOrder(id);
    }
}
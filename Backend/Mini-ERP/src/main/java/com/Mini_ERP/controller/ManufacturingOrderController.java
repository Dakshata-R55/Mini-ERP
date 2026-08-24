package com.Mini_ERP.controller;

import com.Mini_ERP.dto.ManufacturingOrderListResponse;
import com.Mini_ERP.dto.ManufacturingOrderRequest;
import com.Mini_ERP.dto.ManufacturingOrderResponse;
import com.Mini_ERP.dto.ProductResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.ManufacturingOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manufacturing-orders")
@RequiredArgsConstructor
public class ManufacturingOrderController {

    private final ManufacturingOrderService manufacturingOrderService;

    @GetMapping
@RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.READ)
public List<ManufacturingOrderListResponse> listOrders(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Boolean mine,
        @RequestParam(required = false) String search) {
    return manufacturingOrderService.listOrders(status, mine, search);
}

    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.READ)
    public ManufacturingOrderResponse getOrder(@PathVariable Long id) {
        return manufacturingOrderService.getOrder(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public ManufacturingOrderResponse createOrder(@Valid @RequestBody ManufacturingOrderRequest request) {
        return manufacturingOrderService.createOrder(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public ManufacturingOrderResponse updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody ManufacturingOrderRequest request) {
        return manufacturingOrderService.updateOrder(id, request);
    }

    @PostMapping("/{id}/confirm")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public ManufacturingOrderResponse confirmOrder(@PathVariable Long id) {
        return manufacturingOrderService.confirmOrder(id);
    }

    @PostMapping("/{id}/work-orders/{workOrderId}/start")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public ManufacturingOrderResponse startWorkOrder(@PathVariable Long id, @PathVariable Long workOrderId) {
        return manufacturingOrderService.startWorkOrder(id, workOrderId);
    }

    @PostMapping("/{id}/work-orders/{workOrderId}/complete")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public ManufacturingOrderResponse completeWorkOrder(
            @PathVariable Long id,
            @PathVariable Long workOrderId,
            @RequestBody Map<String, Long> body) {
        long duration = body != null && body.get("realDurationMinutes") != null
                ? body.get("realDurationMinutes") : 0L;
        return manufacturingOrderService.completeWorkOrder(id, workOrderId, duration);
    }

    @PostMapping("/{id}/produce")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public ManufacturingOrderResponse produceOrder(@PathVariable Long id) {
        return manufacturingOrderService.produceOrder(id);
    }

    @PostMapping("/{id}/apply-cost")
    @RequiresModuleAccess(module = ErpModule.PRODUCTS, action = RequiresModuleAccess.Action.WRITE)
    public ProductResponse applyProductionCost(@PathVariable Long id) {
        return manufacturingOrderService.applyProductionCostToProduct(id);
    }

    @PostMapping("/{id}/cancel")
    @RequiresModuleAccess(module = ErpModule.MANUFACTURING, action = RequiresModuleAccess.Action.WRITE)
    public ManufacturingOrderResponse cancelOrder(@PathVariable Long id) {
        return manufacturingOrderService.cancelOrder(id);
    }
}

package com.Mini_ERP.service;

import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.BillOfMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcurementOrchestrationService {

    private final BillOfMaterialRepository billOfMaterialRepository;
    private final BomService bomService;
    private final ManufacturingOrderService manufacturingOrderService;
    private final PurchaseOrderService purchaseOrderService;

    @Transactional
    public void handleSalesShortages(SalesOrder salesOrder) {
        Map<Long, BigDecimal> rawShortages = new HashMap<>();

        for (SalesOrderLine line : salesOrder.getLines()) {
            if (!line.isAvailabilityShortage()) {
                continue;
            }

            Product finished = line.getProduct();
            BigDecimal reservedBefore = finished.getReservedQty().subtract(line.getOrderedQty());
            BigDecimal freeBeforeReserve = finished.getOnHandQty().subtract(reservedBefore);
            BigDecimal shortageQty = line.getOrderedQty().subtract(freeBeforeReserve);
            if (shortageQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BillOfMaterial bom = billOfMaterialRepository
                    .findTopByFinishedProductIdAndActiveTrueOrderByCreatedAtDesc(finished.getId())
                    .orElse(null);

            if (bom == null) {
                continue;
            }

            BillOfMaterial detailed = bomService.findWithDetails(bom.getId());

            for (BomComponentLine component : detailed.getComponents()) {
                BigDecimal required = bomService.scaledComponentQty(detailed, component, shortageQty);
                Product raw = component.getComponentProduct();
                BigDecimal rawFree = raw.getFreeToUseQty();
                if (required.compareTo(rawFree) > 0) {
                    BigDecimal shortQty = required.subtract(rawFree);
                    rawShortages.merge(raw.getId(), shortQty, BigDecimal::add);
                }
            }

            manufacturingOrderService.createAndSaveDraftFromBom(
                    finished, shortageQty, salesOrder.getId(), detailed);
        }

        if (!rawShortages.isEmpty()) {
            purchaseOrderService.createDraftForRawShortages(rawShortages);
        }
    }
}

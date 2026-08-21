package com.Mini_ERP.service;

import com.Mini_ERP.model.PurchaseOrder;
import com.Mini_ERP.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseOrderReferenceGenerator {

    private static final String PREFIX = "PO-";

    private final PurchaseOrderRepository purchaseOrderRepository;

    public String nextReference() {
        return purchaseOrderRepository.findTopByOrderByIdDesc()
                .map(PurchaseOrder::getReference)
                .map(this::increment)
                .orElse(PREFIX + "000001");
    }

    private String increment(String lastReference) {
        String numberPart = lastReference.replace(PREFIX, "");
        int next = Integer.parseInt(numberPart) + 1;
        return PREFIX + String.format("%06d", next);
    }
}
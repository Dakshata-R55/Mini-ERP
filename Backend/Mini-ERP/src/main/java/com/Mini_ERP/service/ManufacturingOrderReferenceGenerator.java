package com.Mini_ERP.service;

import com.Mini_ERP.model.ManufacturingOrder;
import com.Mini_ERP.repository.ManufacturingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManufacturingOrderReferenceGenerator {

    private static final String PREFIX = "MO-";

    private final ManufacturingOrderRepository manufacturingOrderRepository;

    public String nextReference() {
        return manufacturingOrderRepository.findTopByOrderByIdDesc()
                .map(ManufacturingOrder::getReference)
                .map(this::increment)
                .orElse(PREFIX + "000001");
    }

    private String increment(String lastReference) {
        String numberPart = lastReference.replace(PREFIX, "");
        int next = Integer.parseInt(numberPart) + 1;
        return PREFIX + String.format("%06d", next);
    }
}

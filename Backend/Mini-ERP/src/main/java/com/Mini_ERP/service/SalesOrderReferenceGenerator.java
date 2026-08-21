package com.Mini_ERP.service;

import com.Mini_ERP.model.SalesOrder;
import com.Mini_ERP.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesOrderReferenceGenerator {

    private static final String PREFIX = "SO-";

    private final SalesOrderRepository salesOrderRepository;

    public String nextReference() {
        return salesOrderRepository.findTopByOrderByIdDesc()
                .map(SalesOrder::getReference)
                .map(this::increment)
                .orElse(PREFIX + "000001");
    }

    private String increment(String lastReference) {
        String numberPart = lastReference.replace(PREFIX, "");
        int next = Integer.parseInt(numberPart) + 1;
        return PREFIX + String.format("%06d", next);
    }
}
package com.Mini_ERP.service;

import com.Mini_ERP.dto.StockLedgerEntryResponse;
import com.Mini_ERP.model.Product;
import com.Mini_ERP.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockLedgerService {

    private final ProductRepository productRepository;

    public List<StockLedgerEntryResponse> listLedger() {
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .map(this::toEntry)
                .toList();
    }

    private StockLedgerEntryResponse toEntry(Product product) {
        BigDecimal stockValue = product.getOnHandQty().multiply(product.getCostPrice());
        return StockLedgerEntryResponse.builder()
                .productId(product.getId())
                .reference(product.getReference())
                .name(product.getName())
                .productType(product.getProductType())
                .onHandQty(product.getOnHandQty())
                .unitCost(product.getCostPrice())
                .stockValue(stockValue)
                .build();
    }
}

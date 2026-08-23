package com.Mini_ERP.service;

import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.ProductRepository;
import com.Mini_ERP.repository.StockMovementRepository;
import com.Mini_ERP.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public Product adjustOnHand(
            Long productId,
            BigDecimal quantityDelta,
            ErpModule sourceModule,
            StockMovementType movementType,
            String documentReference,
            Long documentId) {
        if (quantityDelta == null || quantityDelta.compareTo(BigDecimal.ZERO) == 0) {
            return productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        }

        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        BigDecimal newQty = product.getOnHandQty().add(quantityDelta);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient on-hand stock for product " + product.getReference()
            );
        }

        product.setOnHandQty(newQty);
        Product saved = productRepository.save(product);

        stockMovementRepository.save(StockMovement.builder()
                .product(saved)
                .quantityDelta(quantityDelta)
                .sourceModule(sourceModule)
                .movementType(movementType)
                .documentReference(documentReference)
                .documentId(documentId)
                .createdBy(currentUsername())
                .build());

        return saved;
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getLoginId();
        }
        return "system";
    }
}

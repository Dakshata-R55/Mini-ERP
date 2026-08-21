package com.Mini_ERP.service;

import com.Mini_ERP.dto.ProductListResponse;
import com.Mini_ERP.dto.ProductRequest;
import com.Mini_ERP.dto.ProductResponse;
import com.Mini_ERP.dto.ProductStockRequest;
import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.ProductRepository;
import com.Mini_ERP.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductReferenceGenerator referenceGenerator;
    private final AuditLogService auditLogService;

    public List<ProductListResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .map(this::toListResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = findActiveProduct(id);
        return toResponse(product);
    }

    /**
     * Create product. Project Manager may pass opening stock (On Hand).
     * Other roles → On Hand = 0. Stock also moves via Sales/Purchase/MO later.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request, BigDecimal openingStock) {
        validateProcurementSetup(request);

        BigDecimal onHand = resolveOpeningStock(openingStock);

        Product product = Product.builder()
                .reference(referenceGenerator.nextReference())
                .name(request.getName().trim())
                .salesPrice(request.getSalesPrice())
                .costPrice(request.getCostPrice())
                .onHandQty(onHand)
                .reservedQty(BigDecimal.ZERO)
                .procureOnDemand(request.isProcureOnDemand())
                .procurementType(request.getProcurementType())
                .vendorId(request.getVendorId())
                .vendorName(request.getVendorName())
                .bomId(request.getBomId())
                .bomName(request.getBomName())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        logCreate(saved);
        return toResponse(saved);
    }

    /** Update product master only — never changes On Hand / Reserved. */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validateProcurementSetup(request);

        Product existing = findActiveProduct(id);
        Product before = copyForAudit(existing);

        existing.setName(request.getName().trim());
        existing.setSalesPrice(request.getSalesPrice());
        existing.setCostPrice(request.getCostPrice());
        existing.setProcureOnDemand(request.isProcureOnDemand());
        existing.setProcurementType(request.getProcurementType());
        existing.setVendorId(request.getVendorId());
        existing.setVendorName(request.getVendorName());
        existing.setBomId(request.getBomId());
        existing.setBomName(request.getBomName());
        existing.setImageUrl(request.getImageUrl());

        Product saved = productRepository.save(existing);
        logUpdate(before, saved);
        return toResponse(saved);
    }

    /** Set or adjust On Hand qty — Project Manager (and admins) only. */
    @Transactional
    public ProductResponse updateStock(Long id, ProductStockRequest request) {
        requireStockAccess();

        Product existing = findActiveProduct(id);
        BigDecimal beforeQty = existing.getOnHandQty();
        existing.setOnHandQty(request.getOnHandQty());
        Product saved = productRepository.save(existing);

        auditLogService.logChange(
                ErpModule.PRODUCTS,
                saved.getId(),
                saved.getReference(),
                AuditAction.UPDATE,
                "onHandQty",
                beforeQty,
                saved.getOnHandQty(),
                currentUsername()
        );

        return toResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        requireCatalogDeleteAccess();

        Product product = findActiveProduct(id);
        product.setActive(false);
        productRepository.save(product);

        auditLogService.logChange(
                ErpModule.PRODUCTS,
                product.getId(),
                product.getReference(),
                AuditAction.DELETE,
                "active",
                true,
                false,
                currentUsername()
        );
    }

    private BigDecimal resolveOpeningStock(BigDecimal openingStock) {
        if (openingStock == null || openingStock.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        requireStockAccess();
        return openingStock;
    }

    private void requireStockAccess() {
        if (!canManageStock()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Project Manager can set or change stock quantities"
            );
        }
    }

    private void requireCatalogDeleteAccess() {
        UserType type = currentUserType();
        if (type != UserType.PROJECT_MANAGER
                && type != UserType.SYSTEM_ADMIN
                && type != UserType.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Project Manager can delete products"
            );
        }
    }

    /** Project Manager + admins may write On Hand (opening stock / manual stock). */
    private boolean canManageStock() {
        UserType type = currentUserType();
        return type == UserType.PROJECT_MANAGER
                || type == UserType.SYSTEM_ADMIN
                || type == UserType.ADMIN;
    }

    private UserType currentUserType() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getUserType();
        }
        return UserType.NONE;
    }

    private void validateProcurementSetup(ProductRequest request) {
        if (!request.isProcureOnDemand()) {
            return;
        }

        if (request.getProcurementType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Procurement type is required when Procure on Demand is enabled (MTO)"
            );
        }

        if (request.getProcurementType() == ProcurementType.PURCHASE) {
            if (request.getVendorName() == null || request.getVendorName().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Vendor is required when procurement type is PURCHASE"
                );
            }
        }

        if (request.getProcurementType() == ProcurementType.MANUFACTURING) {
            if (request.getBomName() == null || request.getBomName().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "BoM is required when procurement type is MANUFACTURING"
                );
            }
        }
    }

    private Product findActiveProduct(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void logCreate(Product product) {
        String user = currentUsername();
        auditLogService.logChange(ErpModule.PRODUCTS, product.getId(), product.getReference(),
                AuditAction.CREATE, "name", null, product.getName(), user);
        auditLogService.logChange(ErpModule.PRODUCTS, product.getId(), product.getReference(),
                AuditAction.CREATE, "salesPrice", null, product.getSalesPrice(), user);
        auditLogService.logChange(ErpModule.PRODUCTS, product.getId(), product.getReference(),
                AuditAction.CREATE, "costPrice", null, product.getCostPrice(), user);
        auditLogService.logChange(ErpModule.PRODUCTS, product.getId(), product.getReference(),
                AuditAction.CREATE, "onHandQty", null, product.getOnHandQty(), user);
        auditLogService.logChange(ErpModule.PRODUCTS, product.getId(), product.getReference(),
                AuditAction.CREATE, "procureOnDemand", null, product.isProcureOnDemand(), user);
    }

    private void logUpdate(Product before, Product after) {
        auditField(before, after, "name");
        auditField(before, after, "salesPrice");
        auditField(before, after, "costPrice");
        auditField(before, after, "procureOnDemand");
        auditField(before, after, "procurementType");
        auditField(before, after, "vendorName");
        auditField(before, after, "bomName");
    }

    private void auditField(Product before, Product after, String field) {
        Object oldVal = switch (field) {
            case "name" -> before.getName();
            case "salesPrice" -> before.getSalesPrice();
            case "costPrice" -> before.getCostPrice();
            case "procureOnDemand" -> before.isProcureOnDemand();
            case "procurementType" -> before.getProcurementType();
            case "vendorName" -> before.getVendorName();
            case "bomName" -> before.getBomName();
            default -> null;
        };

        Object newVal = switch (field) {
            case "name" -> after.getName();
            case "salesPrice" -> after.getSalesPrice();
            case "costPrice" -> after.getCostPrice();
            case "procureOnDemand" -> after.isProcureOnDemand();
            case "procurementType" -> after.getProcurementType();
            case "vendorName" -> after.getVendorName();
            case "bomName" -> after.getBomName();
            default -> null;
        };

        auditLogService.logChange(
                ErpModule.PRODUCTS,
                after.getId(),
                after.getReference(),
                AuditAction.UPDATE,
                field,
                oldVal,
                newVal,
                currentUsername()
        );
    }

    private Product copyForAudit(Product p) {
        return Product.builder()
                .id(p.getId())
                .reference(p.getReference())
                .name(p.getName())
                .salesPrice(p.getSalesPrice())
                .costPrice(p.getCostPrice())
                .onHandQty(p.getOnHandQty())
                .reservedQty(p.getReservedQty())
                .procureOnDemand(p.isProcureOnDemand())
                .procurementType(p.getProcurementType())
                .vendorName(p.getVendorName())
                .bomName(p.getBomName())
                .build();
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getLoginId();
        }
        return "system";
    }

    private ProductListResponse toListResponse(Product product) {
        return ProductListResponse.builder()
                .id(product.getId())
                .reference(product.getReference())
                .name(product.getName())
                .salesPrice(product.getSalesPrice())
                .costPrice(product.getCostPrice())
                .onHandQty(product.getOnHandQty())
                .freeToUseQty(product.getFreeToUseQty())
                .procureOnDemand(product.isProcureOnDemand())
                .build();
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .reference(product.getReference())
                .name(product.getName())
                .salesPrice(product.getSalesPrice())
                .costPrice(product.getCostPrice())
                .onHandQty(product.getOnHandQty())
                .reservedQty(product.getReservedQty())
                .freeToUseQty(product.getFreeToUseQty())
                .procurementStrategy(product.getProcurementStrategy())
                .procureOnDemand(product.isProcureOnDemand())
                .procurementType(product.getProcurementType())
                .vendorId(product.getVendorId())
                .vendorName(product.getVendorName())
                .bomId(product.getBomId())
                .bomName(product.getBomName())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
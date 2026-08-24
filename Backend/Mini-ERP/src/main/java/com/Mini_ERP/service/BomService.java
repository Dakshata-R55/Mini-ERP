package com.Mini_ERP.service;

import com.Mini_ERP.dto.*;
import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.BillOfMaterialRepository;
import com.Mini_ERP.repository.ProductRepository;
import com.Mini_ERP.repository.WorkCenterRepository;
import com.Mini_ERP.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BomService {

    private final BillOfMaterialRepository billOfMaterialRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final WorkCenterRepository workCenterRepository;
    private final BomReferenceGenerator referenceGenerator;
    private final AuditLogService auditLogService;

    public List<BomResponse> listBoms() {
        return billOfMaterialRepository.findAllActiveWithProduct().stream()
                .map(this::toResponse)
                .toList();
    }

    public BomResponse getBom(Long id) {
        return toResponse(findWithDetails(id));
    }

    @Transactional
    public BomResponse createBom(BomRequest request) {
        Product finished = resolveFinishedProduct(request);
        BillOfMaterial bom = BillOfMaterial.builder()
                .reference(referenceGenerator.nextReference())
                .finishedProduct(finished)
                .outputQty(request.getOutputQty())
                .active(true)
                .build();

        bom.getComponents().addAll(buildComponents(bom, request.getComponents()));
        bom.getOperations().addAll(buildOperations(bom, request.getOperations()));

        BillOfMaterial saved = billOfMaterialRepository.save(bom);

        auditLogService.logChange(ErpModule.BOM, saved.getId(), saved.getReference(),
                AuditAction.CREATE, "reference", null, saved.getReference(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public BomResponse updateBom(Long id, BomRequest request) {
        BillOfMaterial bom = findWithDetails(id);
        Product finished = resolveFinishedProduct(request);

        bom.setFinishedProduct(finished);
        bom.setOutputQty(request.getOutputQty());
        bom.getComponents().clear();
        bom.getComponents().addAll(buildComponents(bom, request.getComponents()));
        bom.getOperations().clear();
        bom.getOperations().addAll(buildOperations(bom, request.getOperations()));

        BillOfMaterial saved = billOfMaterialRepository.save(bom);
        return toResponse(saved);
    }

    @Transactional
    public void deleteBom(Long id) {
        BillOfMaterial bom = findWithDetails(id);
        bom.setActive(false);
        billOfMaterialRepository.save(bom);

        auditLogService.logChange(ErpModule.BOM, bom.getId(), bom.getReference(),
                AuditAction.DELETE, "active", true, false, currentUsername());
    }

    public BillOfMaterial findWithDetails(Long id) {
        BillOfMaterial bom = billOfMaterialRepository.findActiveWithComponents(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BOM not found"));
        billOfMaterialRepository.findActiveWithOperations(id);
        return bom;
    }

    private Product findFinishedProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        if (product.getProductType() != ProductType.FINISHED_GOOD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "BOM finished product must be a finished good");
        }
        return product;
    }

    private Product resolveFinishedProduct(BomRequest request) {
        if (request.getFinishedProductId() != null
                && (request.getFinishedProductName() == null || request.getFinishedProductName().isBlank())) {
            Product product = findFinishedProduct(request.getFinishedProductId());
            return productService.findOrCreateForBom(
                    product.getName(),
                    ProductType.FINISHED_GOOD,
                    null);
        }

        String name = request.getFinishedProductName();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Finished product name is required");
        }

        return productService.findOrCreateForBom(name, ProductType.FINISHED_GOOD, null);
    }

    private Product resolveComponentProduct(BomComponentRequest req) {
        if (req.getProductId() != null) {
            Product component = productRepository.findById(req.getProductId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Component product not found: " + req.getProductId()));

            if (component.getProductType() != ProductType.RAW_MATERIAL) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "BOM components must be raw materials: " + component.getReference());
            }
            return component;
        }

        String name = req.getProductName();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Raw material name is required");
        }

        return productService.findOrCreateForBom(name, ProductType.RAW_MATERIAL, null);
    }

    private List<BomComponentLine> buildComponents(BillOfMaterial bom, List<BomComponentRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BOM must have at least one component");
        }

        return requests.stream()
                .map(req -> {
                    Product component = resolveComponentProduct(req);

                    return BomComponentLine.builder()
                            .billOfMaterial(bom)
                            .componentProduct(component)
                            .qtyPerOutput(req.getQtyPerOutput())
                            .build();
                })
                .toList();
    }

    private List<BomOperation> buildOperations(BillOfMaterial bom, List<BomOperationRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(req -> {
                    WorkCenter workCenter = workCenterRepository.findByIdAndActiveTrue(req.getWorkCenterId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Work center not found: " + req.getWorkCenterId()));

                    return BomOperation.builder()
                            .billOfMaterial(bom)
                            .workCenter(workCenter)
                            .sequence(req.getSequence())
                            .expectedDurationMinutes(req.getExpectedDurationMinutes())
                            .build();
                })
                .toList();
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getLoginId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private BomResponse toResponse(BillOfMaterial bom) {
        return BomResponse.builder()
                .id(bom.getId())
                .reference(bom.getReference())
                .finishedProductId(bom.getFinishedProduct().getId())
                .finishedProductName(bom.getFinishedProduct().getName())
                .outputQty(bom.getOutputQty())
                .components(bom.getComponents().stream().map(this::toComponentResponse).toList())
                .operations(bom.getOperations().stream().map(this::toOperationResponse).toList())
                .build();
    }

    private BomComponentResponse toComponentResponse(BomComponentLine line) {
        return BomComponentResponse.builder()
                .id(line.getId())
                .productId(line.getComponentProduct().getId())
                .productName(line.getComponentProduct().getName())
                .qtyPerOutput(line.getQtyPerOutput())
                .build();
    }

    private BomOperationResponse toOperationResponse(BomOperation op) {
        return BomOperationResponse.builder()
                .id(op.getId())
                .workCenterId(op.getWorkCenter().getId())
                .workCenterName(op.getWorkCenter().getName())
                .location(op.getWorkCenter().getLocation())
                .sequence(op.getSequence())
                .expectedDurationMinutes(op.getExpectedDurationMinutes())
                .build();
    }

    /** Scale component qty for a given production quantity */
    public BigDecimal scaledComponentQty(BillOfMaterial bom, BomComponentLine component, BigDecimal qtyToProduce) {
        return qtyToProduce
                .multiply(component.getQtyPerOutput())
                .divide(bom.getOutputQty(), 4, RoundingMode.HALF_UP);
    }
}

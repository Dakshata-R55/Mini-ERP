package com.Mini_ERP.service;

import com.Mini_ERP.dto.*;
import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.BillOfMaterialRepository;
import com.Mini_ERP.repository.ManufacturingOrderRepository;
import com.Mini_ERP.repository.ProductRepository;
import com.Mini_ERP.repository.UserRepository;
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
public class ManufacturingOrderService {

    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BillOfMaterialRepository billOfMaterialRepository;
    private final ManufacturingOrderReferenceGenerator referenceGenerator;
    private final BomService bomService;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;

   public List<ManufacturingOrderListResponse> listOrders(String status, Boolean mine, String search) {
    ManufacturingOrderStatus statusFilter = parseStatus(status);
    Long currentUserId = Boolean.TRUE.equals(mine) ? currentUserId() : null;

    return manufacturingOrderRepository.findAllActiveWithAssignee().stream()
            .filter(mo -> statusFilter == null || mo.getStatus() == statusFilter)
            .filter(mo -> currentUserId == null || belongsToAssignee(mo, currentUserId))
            .filter(mo -> matchesSearch(mo, search))
            .map(this::toListResponse)
            .toList();
}

private ManufacturingOrderStatus parseStatus(String status) {
    if (status == null || status.isBlank()) {
        return null;
    }
    try {
        return ManufacturingOrderStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
    }
}

private boolean belongsToAssignee(ManufacturingOrder order, Long userId) {
    return order.getAssignee() != null && order.getAssignee().getId().equals(userId);
}

private boolean matchesSearch(ManufacturingOrder order, String search) {
    if (search == null || search.isBlank()) {
        return true;
    }
    String q = search.trim().toLowerCase();
    return order.getReference().toLowerCase().contains(q)
            || order.getFinishedProduct().getName().toLowerCase().contains(q);
}

private Long currentUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
        return details.getUser().getId();
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
}

    public ManufacturingOrderResponse getOrder(Long id) {
        return toResponse(findWithDetails(id));
    }

    @Transactional
    public ManufacturingOrderResponse createOrder(ManufacturingOrderRequest request) {
        ManufacturingOrder order = buildOrder(request);
        ManufacturingOrder saved = manufacturingOrderRepository.save(order);

        auditLogService.logChange(ErpModule.MANUFACTURING, saved.getId(), saved.getReference(),
                AuditAction.CREATE, "status", null, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public ManufacturingOrderResponse updateOrder(Long id, ManufacturingOrderRequest request) {
        ManufacturingOrder order = findWithDetails(id);
        ensureDraft(order);

        Product finished = findFinishedProduct(request.getFinishedProductId());
        order.setFinishedProduct(finished);
        order.setQtyToProduce(request.getQtyToProduce());
        order.setAssignee(resolveAssignee(request.getAssigneeId()));
        order.setSalesOrderId(request.getSalesOrderId());

        BillOfMaterial bom = resolveBom(request.getBomId(), finished.getId());
        order.setBillOfMaterial(bom);
        repopulateFromBom(order, bom);

        ManufacturingOrder saved = manufacturingOrderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public ManufacturingOrderResponse confirmOrder(Long id) {
        ManufacturingOrder order = findWithDetails(id);
        ensureDraft(order);

        if (order.getComponents().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Manufacturing order must have component lines (define a BOM first)");
        }

        ManufacturingOrderStatus before = order.getStatus();
        order.setStatus(ManufacturingOrderStatus.CONFIRMED);
        ManufacturingOrder saved = manufacturingOrderRepository.save(order);

        auditLogService.logChange(ErpModule.MANUFACTURING, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", before, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public ManufacturingOrderResponse startWorkOrder(Long orderId, Long workOrderId) {
        ManufacturingOrder order = findWithDetails(orderId);
        if (order.getStatus() != ManufacturingOrderStatus.CONFIRMED
                && order.getStatus() != ManufacturingOrderStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must be confirmed before starting work");
        }

        MoWorkOrder workOrder = order.getWorkOrders().stream()
                .filter(wo -> wo.getId().equals(workOrderId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work order not found"));

        if (workOrder.getStatus() != MoWorkOrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work order is not pending");
        }

        workOrder.setStatus(MoWorkOrderStatus.IN_PROGRESS);
        order.setStatus(ManufacturingOrderStatus.IN_PROGRESS);
        ManufacturingOrder saved = manufacturingOrderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public ManufacturingOrderResponse completeWorkOrder(Long orderId, Long workOrderId, long realDurationMinutes) {
        ManufacturingOrder order = findWithDetails(orderId);
        MoWorkOrder workOrder = order.getWorkOrders().stream()
                .filter(wo -> wo.getId().equals(workOrderId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work order not found"));

        if (workOrder.getStatus() != MoWorkOrderStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work order must be in progress");
        }

        workOrder.setStatus(MoWorkOrderStatus.DONE);
        workOrder.setRealDurationMinutes(realDurationMinutes);

        boolean allDone = order.getWorkOrders().isEmpty()
                || order.getWorkOrders().stream().allMatch(wo -> wo.getStatus() == MoWorkOrderStatus.DONE);
        if (allDone) {
            order.setStatus(ManufacturingOrderStatus.TO_CLOSE);
        }

        ManufacturingOrder saved = manufacturingOrderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public ManufacturingOrderResponse produceOrder(Long id) {
        ManufacturingOrder order = findWithDetails(id);
        if (order.getStatus() != ManufacturingOrderStatus.TO_CLOSE
                && order.getStatus() != ManufacturingOrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order must be confirmed (no operations) or ready to close");
        }

        if (!order.getWorkOrders().isEmpty()
                && order.getWorkOrders().stream().anyMatch(wo -> wo.getStatus() != MoWorkOrderStatus.DONE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All work orders must be completed first");
        }

        BigDecimal productionCost = BigDecimal.ZERO;

        for (MoComponentLine componentLine : order.getComponents()) {
            BigDecimal toConsume = componentLine.getToConsumeQty();
            Product raw = componentLine.getProduct();

            inventoryService.adjustOnHand(
                    raw.getId(),
                    toConsume.negate(),
                    ErpModule.MANUFACTURING,
                    StockMovementType.MANUFACTURING_CONSUME,
                    order.getReference(),
                    order.getId());

            componentLine.setConsumedQty(toConsume);
            productionCost = productionCost.add(toConsume.multiply(raw.getCostPrice()));

            productRepository.save(raw);
        }

        inventoryService.adjustOnHand(
                order.getFinishedProduct().getId(),
                order.getQtyToProduce(),
                ErpModule.MANUFACTURING,
                StockMovementType.MANUFACTURING_PRODUCE,
                order.getReference(),
                order.getId());

        order.setTotalProductionCost(productionCost.setScale(2, RoundingMode.HALF_UP));
        order.setStatus(ManufacturingOrderStatus.DONE);
        ManufacturingOrder saved = manufacturingOrderRepository.save(order);

        auditLogService.logChange(ErpModule.MANUFACTURING, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", ManufacturingOrderStatus.TO_CLOSE, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse applyProductionCostToProduct(Long id) {
        ManufacturingOrder order = findWithDetails(id);
        if (order.getStatus() != ManufacturingOrderStatus.DONE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must be produced before applying cost");
        }
        if (order.getTotalProductionCost() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No production cost recorded");
        }

        Product finished = order.getFinishedProduct();
        BigDecimal unitCost = order.getTotalProductionCost()
                .divide(order.getQtyToProduce(), 4, RoundingMode.HALF_UP);
        finished.setCostPrice(unitCost);
        productRepository.save(finished);

        auditLogService.logChange(ErpModule.PRODUCTS, finished.getId(), finished.getReference(),
                AuditAction.UPDATE, "costPrice", null, unitCost, currentUsername());

        return ProductResponse.builder()
                .id(finished.getId())
                .reference(finished.getReference())
                .name(finished.getName())
                .productType(finished.getProductType())
                .salesPrice(finished.getSalesPrice())
                .costPrice(finished.getCostPrice())
                .onHandQty(finished.getOnHandQty())
                .reservedQty(finished.getReservedQty())
                .freeToUseQty(finished.getFreeToUseQty())
                .procureOnDemand(finished.isProcureOnDemand())
                .procurementType(finished.getProcurementType())
                .vendorId(finished.getVendorId())
                .vendorName(finished.getVendorName())
                .bomId(finished.getBomId())
                .bomName(finished.getBomName())
                .imageUrl(finished.getImageUrl())
                .active(finished.isActive())
                .build();
    }

    @Transactional
    public ManufacturingOrderResponse cancelOrder(Long id) {
        ManufacturingOrder order = findWithDetails(id);
        if (order.getStatus() == ManufacturingOrderStatus.DONE
                || order.getStatus() == ManufacturingOrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order cannot be cancelled");
        }

        ManufacturingOrderStatus before = order.getStatus();
        order.setStatus(ManufacturingOrderStatus.CANCELLED);
        ManufacturingOrder saved = manufacturingOrderRepository.save(order);

        auditLogService.logChange(ErpModule.MANUFACTURING, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", before, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public ManufacturingOrder createAndSaveDraftFromBom(
            Product finishedProduct,
            BigDecimal qtyToProduce,
            Long salesOrderId,
            BillOfMaterial bom) {
        ManufacturingOrderRequest request = new ManufacturingOrderRequest();
        request.setFinishedProductId(finishedProduct.getId());
        request.setQtyToProduce(qtyToProduce);
        request.setBomId(bom.getId());
        request.setSalesOrderId(salesOrderId);
        ManufacturingOrder order = buildOrder(request);
        ManufacturingOrder saved = manufacturingOrderRepository.save(order);
        auditLogService.logChange(ErpModule.MANUFACTURING, saved.getId(), saved.getReference(),
                AuditAction.CREATE, "status", null, saved.getStatus(), "system");
        return saved;
    }

    private ManufacturingOrder buildOrder(ManufacturingOrderRequest request) {
        Product finished = findFinishedProduct(request.getFinishedProductId());
        BillOfMaterial bom = resolveBom(request.getBomId(), finished.getId());

        ManufacturingOrder order = ManufacturingOrder.builder()
                .reference(referenceGenerator.nextReference())
                .status(ManufacturingOrderStatus.DRAFT)
                .finishedProduct(finished)
                .qtyToProduce(request.getQtyToProduce())
                .billOfMaterial(bom)
                .assignee(resolveAssignee(request.getAssigneeId()))
                .salesOrderId(request.getSalesOrderId())
                .active(true)
                .build();

        repopulateFromBom(order, bom);
        return order;
    }

    private void repopulateFromBom(ManufacturingOrder order, BillOfMaterial bom) {
        order.getComponents().clear();
        order.getWorkOrders().clear();

        if (bom == null) {
            return;
        }

        BillOfMaterial detailed = bomService.findWithDetails(bom.getId());

        for (BomComponentLine component : detailed.getComponents()) {
            BigDecimal toConsume = bomService.scaledComponentQty(detailed, component, order.getQtyToProduce());
            order.getComponents().add(MoComponentLine.builder()
                    .manufacturingOrder(order)
                    .product(component.getComponentProduct())
                    .toConsumeQty(toConsume)
                    .consumedQty(BigDecimal.ZERO)
                    .build());
        }

        for (BomOperation operation : detailed.getOperations()) {
            order.getWorkOrders().add(MoWorkOrder.builder()
                    .manufacturingOrder(order)
                    .workCenter(operation.getWorkCenter())
                    .sequence(operation.getSequence())
                    .expectedDurationMinutes(operation.getExpectedDurationMinutes())
                    .realDurationMinutes(0)
                    .status(MoWorkOrderStatus.PENDING)
                    .build());
        }
    }

    private BillOfMaterial resolveBom(Long bomId, Long finishedProductId) {
        if (bomId != null) {
            BillOfMaterial bom = billOfMaterialRepository.findActiveWithDetails(bomId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BOM not found"));
            if (!bom.getFinishedProduct().getId().equals(finishedProductId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "BOM does not match the finished product");
            }
            return bom;
        }

        return billOfMaterialRepository
                .findTopByFinishedProductIdAndActiveTrueOrderByCreatedAtDesc(finishedProductId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active BOM found for this finished product"));
    }

    private Product findFinishedProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        if (product.getProductType() != ProductType.FINISHED_GOOD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Manufacturing order product must be a finished good");
        }
        return product;
    }

    private AppUser resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .filter(AppUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
    }

    private ManufacturingOrder findWithDetails(Long id) {
        return manufacturingOrderRepository.findActiveWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manufacturing order not found"));
    }

    private void ensureDraft(ManufacturingOrder order) {
        if (order.getStatus() != ManufacturingOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft orders can be edited");
        }
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getLoginId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private ManufacturingOrderListResponse toListResponse(ManufacturingOrder order) {
        return ManufacturingOrderListResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .finishedProductName(order.getFinishedProduct().getName())
                .qtyToProduce(order.getQtyToProduce())
                .assigneeName(order.getAssignee() != null ? order.getAssignee().getFullName() : null)
                .build();
    }

    private ManufacturingOrderResponse toResponse(ManufacturingOrder order) {
        return ManufacturingOrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .finishedProductId(order.getFinishedProduct().getId())
                .finishedProductName(order.getFinishedProduct().getName())
                .qtyToProduce(order.getQtyToProduce())
                .bomId(order.getBillOfMaterial() != null ? order.getBillOfMaterial().getId() : null)
                .bomReference(order.getBillOfMaterial() != null ? order.getBillOfMaterial().getReference() : null)
                .assigneeId(order.getAssignee() != null ? order.getAssignee().getId() : null)
                .assigneeName(order.getAssignee() != null ? order.getAssignee().getFullName() : null)
                .salesOrderId(order.getSalesOrderId())
                .totalProductionCost(order.getTotalProductionCost())
                .components(order.getComponents().stream().map(this::toComponentResponse).toList())
                .workOrders(order.getWorkOrders().stream().map(this::toWorkOrderResponse).toList())
                .build();
    }

    private MoComponentResponse toComponentResponse(MoComponentLine line) {
        return MoComponentResponse.builder()
                .id(line.getId())
                .productId(line.getProduct().getId())
                .productName(line.getProduct().getName())
                .toConsumeQty(line.getToConsumeQty())
                .consumedQty(line.getConsumedQty())
                .build();
    }

    private MoWorkOrderResponse toWorkOrderResponse(MoWorkOrder wo) {
        return MoWorkOrderResponse.builder()
                .id(wo.getId())
                .workCenterId(wo.getWorkCenter().getId())
                .workCenterName(wo.getWorkCenter().getName())
                .location(wo.getWorkCenter().getLocation())
                .sequence(wo.getSequence())
                .expectedDurationMinutes(wo.getExpectedDurationMinutes())
                .realDurationMinutes(wo.getRealDurationMinutes())
                .status(wo.getStatus())
                .build();
    }
}

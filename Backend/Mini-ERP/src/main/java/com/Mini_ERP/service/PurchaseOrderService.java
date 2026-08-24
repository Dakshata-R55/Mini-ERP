package com.Mini_ERP.service;

import com.Mini_ERP.dto.*;
import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.ProductRepository;
import com.Mini_ERP.repository.PurchaseOrderRepository;
import com.Mini_ERP.repository.UserRepository;
import com.Mini_ERP.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorService vendorService;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final PurchaseOrderReferenceGenerator referenceGenerator;
    private final AuditLogService auditLogService;
    private final InventoryService inventoryService;

   public List<PurchaseOrderListResponse> listOrders(String status, Boolean late, Boolean mine, String search) {
    PurchaseOrderStatus statusFilter = parseStatus(status);
    Long currentUserId = Boolean.TRUE.equals(mine) ? currentUserId() : null;

    return purchaseOrderRepository.findAllActiveWithVendor().stream()
            .filter(po -> statusFilter == null || po.getStatus() == statusFilter)
            .filter(po -> late == null || !late || isLate(po))
            .filter(po -> currentUserId == null || belongsToResponsible(po, currentUserId))
            .filter(po -> matchesSearch(po, search))
            .map(this::toListResponse)
            .toList();
}

    public PurchaseOrderResponse getOrder(Long id) {
        return toResponse(findWithDetails(id));
    }

    @Transactional
    public PurchaseOrderResponse createOrder(PurchaseOrderRequest request) {
        Vendor vendor = vendorService.findActive(request.getVendorId());
        AppUser responsiblePerson = resolveResponsiblePerson(request.getResponsiblePersonId());

        PurchaseOrder order = PurchaseOrder.builder()
                .reference(referenceGenerator.nextReference())
                .status(PurchaseOrderStatus.DRAFT)
                .vendor(vendor)
                .vendorAddress(resolveAddress(request.getVendorAddress(), vendor))
                .startDate(request.getStartDate())
                .responsiblePerson(responsiblePerson)
                .active(true)
                .build();

        order.getLines().addAll(buildLines(order, request.getLines()));
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        auditLogService.logChange(ErpModule.PURCHASE, saved.getId(), saved.getReference(),
                AuditAction.CREATE, "status", null, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse updateOrder(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = findWithDetails(id);
        ensureDraft(order);

        Vendor vendor = vendorService.findActive(request.getVendorId());
        order.setVendor(vendor);
        order.setVendorAddress(resolveAddress(request.getVendorAddress(), vendor));
        order.setStartDate(request.getStartDate());
        order.setResponsiblePerson(resolveResponsiblePerson(request.getResponsiblePersonId()));

        order.getLines().clear();
        order.getLines().addAll(buildLines(order, request.getLines()));

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse confirmOrder(Long id) {
        PurchaseOrder order = findWithDetails(id);
        ensureDraft(order);

        if (order.getLines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase order must have at least one line");
        }

        PurchaseOrderStatus before = order.getStatus();
        order.setStatus(PurchaseOrderStatus.CONFIRMED);
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        auditLogService.logChange(ErpModule.PURCHASE, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", before, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse receiveOrder(Long id, ReceivePurchaseOrderRequest request) {
        PurchaseOrder order = findWithDetails(id);
        if (order.getStatus() != PurchaseOrderStatus.CONFIRMED
                && order.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must be confirmed before receiving");
        }

        Map<Long, PurchaseOrderLine> lineMap = order.getLines().stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, Function.identity()));

        for (ReceivePurchaseOrderRequest.LineReceive receive : request.getLines()) {
            PurchaseOrderLine line = lineMap.get(receive.getLineId());
            if (line == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid line id: " + receive.getLineId());
            }

            BigDecimal newReceived = receive.getReceivedQty();
            if (newReceived.compareTo(line.getReceivedQty()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received qty cannot decrease");
            }
            if (newReceived.compareTo(line.getOrderedQty()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received qty cannot exceed ordered qty");
            }

            BigDecimal delta = newReceived.subtract(line.getReceivedQty());
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal beforeQty = line.getReceivedQty();
                inventoryService.adjustOnHand(
                        line.getProduct().getId(),
                        delta,
                        ErpModule.PURCHASE,
                        StockMovementType.PURCHASE_RECEIVE,
                        order.getReference(),
                        order.getId());
                line.setReceivedQty(newReceived);

                auditLogService.logChange(ErpModule.PURCHASE, order.getId(), order.getReference(),
                        AuditAction.UPDATE, "receivedQty", beforeQty, newReceived, currentUsername());
            }
        }

        order.setStatus(resolveReceiveStatus(order));
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        auditLogService.logChange(ErpModule.PURCHASE, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", null, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse cancelOrder(Long id) {
        PurchaseOrder order = findWithDetails(id);

        if (order.getStatus() == PurchaseOrderStatus.CANCELLED
                || order.getStatus() == PurchaseOrderStatus.FULLY_RECEIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order cannot be cancelled");
        }

        if (order.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            reverseReceivedStock(order);
        }

        PurchaseOrderStatus before = order.getStatus();
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        auditLogService.logChange(ErpModule.PURCHASE, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", before, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrder createDraftForRawShortages(Map<Long, BigDecimal> rawShortages) {
        if (rawShortages == null || rawShortages.isEmpty()) {
            return null;
        }

        Vendor vendor = vendorService.findFirstActiveOrNull();
        if (vendor == null) {
            return null;
        }

        List<PurchaseOrderLineRequest> lineRequests = rawShortages.entrySet().stream()
                .map(entry -> {
                    PurchaseOrderLineRequest req = new PurchaseOrderLineRequest();
                    req.setProductId(entry.getKey());
                    req.setOrderedQty(entry.getValue());
                    return req;
                })
                .toList();

        PurchaseOrder order = PurchaseOrder.builder()
                .reference(referenceGenerator.nextReference())
                .status(PurchaseOrderStatus.DRAFT)
                .vendor(vendor)
                .vendorAddress(resolveAddress(null, vendor))
                .startDate(LocalDate.now())
                .active(true)
                .build();

        order.getLines().addAll(buildLines(order, lineRequests));
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        auditLogService.logChange(ErpModule.PURCHASE, saved.getId(), saved.getReference(),
                AuditAction.CREATE, "status", null, saved.getStatus(), "system");
        return saved;
    }

    @Transactional
    public void deleteOrder(Long id) {
        PurchaseOrder order = findWithDetails(id);
        ensureDraft(order);
        order.setActive(false);
        purchaseOrderRepository.save(order);

        auditLogService.logChange(ErpModule.PURCHASE, order.getId(), order.getReference(),
                AuditAction.DELETE, "active", true, false, currentUsername());
    }

    private List<PurchaseOrderLine> buildLines(PurchaseOrder order, List<PurchaseOrderLineRequest> lineRequests) {
        return lineRequests.stream()
                .map(req -> {
                    Product product = resolveLineProduct(req);

                    BigDecimal unitCostPrice = req.getUnitCostPrice() != null
                            ? req.getUnitCostPrice()
                            : product.getCostPrice();

                    return PurchaseOrderLine.builder()
                            .purchaseOrder(order)
                            .product(product)
                            .orderedQty(req.getOrderedQty())
                            .receivedQty(BigDecimal.ZERO)
                            .unitCostPrice(unitCostPrice)
                            .units(req.getUnits())
                            .build();
                })
                .toList();
    }

<<<<<<< HEAD
private boolean belongsToResponsible(PurchaseOrder order, Long userId) {
    return order.getResponsiblePerson() != null && order.getResponsiblePerson().getId().equals(userId);
}

private Long currentUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
        return details.getUser().getId();
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
}
=======
    private Product resolveLineProduct(PurchaseOrderLineRequest req) {
        if (req.getProductId() != null) {
            Product product = productRepository.findById(req.getProductId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Product not found: " + req.getProductId()));

            if (product.getProductType() != ProductType.RAW_MATERIAL) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Purchase orders can only include raw material products. "
                                + product.getReference() + " is a finished good."
                );
            }
            return product;
        }

        String name = req.getProductName();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Raw material name is required");
        }

        return productService.findOrCreateRawMaterialForPurchase(name, req.getUnitCostPrice());
    }
>>>>>>> 1d1cc83 (Fix Bug in ui)

    private void reverseReceivedStock(PurchaseOrder order) {
        for (PurchaseOrderLine line : order.getLines()) {
            if (line.getReceivedQty().compareTo(BigDecimal.ZERO) > 0) {
                inventoryService.adjustOnHand(
                        line.getProduct().getId(),
                        line.getReceivedQty().negate(),
                        ErpModule.PURCHASE,
                        StockMovementType.PURCHASE_REVERSE,
                        order.getReference(),
                        order.getId());
            }
        }
    }

    private PurchaseOrderStatus resolveReceiveStatus(PurchaseOrder order) {
        boolean allReceived = order.getLines().stream()
                .allMatch(line -> line.getReceivedQty().compareTo(line.getOrderedQty()) == 0);
        if (allReceived) {
            return PurchaseOrderStatus.FULLY_RECEIVED;
        }
        boolean anyReceived = order.getLines().stream()
                .anyMatch(line -> line.getReceivedQty().compareTo(BigDecimal.ZERO) > 0);
        return anyReceived ? PurchaseOrderStatus.PARTIALLY_RECEIVED : order.getStatus();
    }

    private boolean isLate(PurchaseOrder order) {
        return (order.getStatus() == PurchaseOrderStatus.CONFIRMED
                || order.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED)
                && order.getStartDate().isBefore(LocalDate.now());
    }

    private boolean matchesSearch(PurchaseOrder order, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String q = search.trim().toLowerCase();
        return order.getReference().toLowerCase().contains(q)
                || order.getVendor().getName().toLowerCase().contains(q);
    }

    private PurchaseOrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PurchaseOrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    private String resolveAddress(String requested, Vendor vendor) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim();
        }
        return vendor.getAddress() != null ? vendor.getAddress() : "";
    }

    private AppUser resolveResponsiblePerson(Long responsiblePersonId) {
        if (responsiblePersonId == null) {
            return null;
        }
        return userRepository.findById(responsiblePersonId)
                .filter(AppUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Responsible person not found"));
    }

    private PurchaseOrder findWithDetails(Long id) {
        return purchaseOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    private void ensureDraft(PurchaseOrder order) {
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
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

    private BigDecimal calculateOrderTotal(PurchaseOrder order) {
        return order.getLines().stream()
                .map(PurchaseOrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PurchaseOrderListResponse toListResponse(PurchaseOrder order) {
        return PurchaseOrderListResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .startDate(order.getStartDate())
                .vendorName(order.getVendor().getName())
                .responsiblePersonName(order.getResponsiblePerson() != null
                        ? order.getResponsiblePerson().getFullName() : null)
                .status(order.getStatus())
                .orderTotal(calculateOrderTotal(order))
                .build();
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order) {
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .vendorId(order.getVendor().getId())
                .vendorName(order.getVendor().getName())
                .vendorAddress(order.getVendorAddress())
                .creationDate(order.getCreationDate())
                .startDate(order.getStartDate())
                .responsiblePersonId(order.getResponsiblePerson() != null
                        ? order.getResponsiblePerson().getId() : null)
                .responsiblePersonName(order.getResponsiblePerson() != null
                        ? order.getResponsiblePerson().getFullName() : null)
                .orderTotal(calculateOrderTotal(order))
                .lines(order.getLines().stream().map(this::toLineResponse).toList())
                .build();
    }

    private PurchaseOrderLineResponse toLineResponse(PurchaseOrderLine line) {
        return PurchaseOrderLineResponse.builder()
                .id(line.getId())
                .productId(line.getProduct().getId())
                .productReference(line.getProduct().getReference())
                .productName(line.getProduct().getName())
                .orderedQty(line.getOrderedQty())
                .receivedQty(line.getReceivedQty())
                .unitCostPrice(line.getUnitCostPrice())
                .units(line.getUnits())
                .lineTotal(line.getLineTotal())
                .build();
    }
}
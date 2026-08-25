package com.Mini_ERP.service;

import com.Mini_ERP.dto.*;
import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.ProductRepository;
import com.Mini_ERP.repository.SalesOrderRepository;
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
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SalesOrderReferenceGenerator referenceGenerator;
    private final AuditLogService auditLogService;
    private final InventoryService inventoryService;
    private final ProcurementOrchestrationService procurementOrchestrationService;

   public List<SalesOrderListResponse> listOrders(String status, Boolean late, Boolean mine, String search) {
    SalesOrderStatus statusFilter = parseStatus(status);
    Long currentUserId = Boolean.TRUE.equals(mine) ? currentUserId() : null;

    return salesOrderRepository.findAllActiveWithCustomer().stream()
            .filter(so -> statusFilter == null || so.getStatus() == statusFilter)
            .filter(so -> late == null || !late || isLate(so))
            .filter(so -> currentUserId == null || belongsToSalesPerson(so, currentUserId))
            .filter(so -> matchesSearch(so, search))
            .map(this::toListResponse)
            .toList();
}

    public SalesOrderResponse getOrder(Long id) {
        return toResponse(findWithDetails(id));
    }

    @Transactional
    public SalesOrderResponse createOrder(SalesOrderRequest request) {
        Customer customer = customerService.findActive(request.getCustomerId());
        AppUser salesPerson = resolveSalesPerson(request.getSalesPersonId());

        SalesOrder order = SalesOrder.builder()
                .reference(referenceGenerator.nextReference())
                .status(SalesOrderStatus.DRAFT)
                .customer(customer)
                .customerAddress(resolveAddress(request.getCustomerAddress(), customer))
                .startDate(request.getStartDate())
                .salesPerson(salesPerson)
                .active(true)
                .build();

        order.getLines().addAll(buildLines(order, request.getLines()));
        SalesOrder saved = salesOrderRepository.save(order);

        auditLogService.logChange(ErpModule.SALES, saved.getId(), saved.getReference(),
                AuditAction.CREATE, "status", null, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public SalesOrderResponse updateOrder(Long id, SalesOrderRequest request) {
        SalesOrder order = findWithDetails(id);
        ensureDraft(order);

        Customer customer = customerService.findActive(request.getCustomerId());
        order.setCustomer(customer);
        order.setCustomerAddress(resolveAddress(request.getCustomerAddress(), customer));
        order.setStartDate(request.getStartDate());
        order.setSalesPerson(resolveSalesPerson(request.getSalesPersonId()));

        order.getLines().clear();
        order.getLines().addAll(buildLines(order, request.getLines()));

        SalesOrder saved = salesOrderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public SalesOrderResponse confirmOrder(Long id) {
        SalesOrder order = findWithDetails(id);
        ensureDraft(order);

        if (order.getLines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sales order must have at least one line");
        }

        for (SalesOrderLine line : order.getLines()) {
            Product product = line.getProduct();
            boolean shortage = line.getOrderedQty().compareTo(product.getFreeToUseQty()) > 0;
            line.setAvailabilityShortage(shortage);
            product.setReservedQty(product.getReservedQty().add(line.getOrderedQty()));
            productRepository.save(product);
        }

        SalesOrderStatus before = order.getStatus();
        order.setStatus(SalesOrderStatus.CONFIRMED);
        SalesOrder saved = salesOrderRepository.save(order);

        auditLogService.logChange(ErpModule.SALES, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", before, saved.getStatus(), currentUsername());

        procurementOrchestrationService.handleSalesShortages(saved);
        return toResponse(findWithDetails(saved.getId()));
    }

    @Transactional
    public SalesOrderResponse deliverOrder(Long id, DeliverSalesOrderRequest request) {
        SalesOrder order = findWithDetails(id);
        if (order.getStatus() != SalesOrderStatus.CONFIRMED
                && order.getStatus() != SalesOrderStatus.PARTIALLY_DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must be confirmed before delivery");
        }

        Map<Long, SalesOrderLine> lineMap = order.getLines().stream()
                .collect(Collectors.toMap(SalesOrderLine::getId, Function.identity()));

        for (DeliverSalesOrderRequest.LineDelivery delivery : request.getLines()) {
            SalesOrderLine line = lineMap.get(delivery.getLineId());
            if (line == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid line id: " + delivery.getLineId());
            }

            BigDecimal newDelivered = delivery.getDeliveredQty();
            if (newDelivered.compareTo(line.getDeliveredQty()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivered qty cannot decrease");
            }
            if (newDelivered.compareTo(line.getOrderedQty()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivered qty cannot exceed ordered qty");
            }

            BigDecimal delta = newDelivered.subtract(line.getDeliveredQty());
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                Product product = line.getProduct();
                inventoryService.adjustOnHand(
                        product.getId(),
                        delta.negate(),
                        ErpModule.SALES,
                        StockMovementType.SALES_DELIVER,
                        order.getReference(),
                        order.getId());
                product.setReservedQty(product.getReservedQty().subtract(delta));
                productRepository.save(product);
                line.setDeliveredQty(newDelivered);

                auditLogService.logChange(ErpModule.SALES, order.getId(), order.getReference(),
                        AuditAction.UPDATE, "deliveredQty", line.getDeliveredQty().subtract(delta),
                        newDelivered, currentUsername());
            }
        }

        order.setStatus(resolveDeliveryStatus(order));
        SalesOrder saved = salesOrderRepository.save(order);

        auditLogService.logChange(ErpModule.SALES, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", null, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public SalesOrderResponse cancelOrder(Long id) {
        SalesOrder order = findWithDetails(id);

        if (order.getStatus() == SalesOrderStatus.CANCELLED
                || order.getStatus() == SalesOrderStatus.FULLY_DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order cannot be cancelled");
        }

        if (order.getStatus() == SalesOrderStatus.CONFIRMED
                || order.getStatus() == SalesOrderStatus.PARTIALLY_DELIVERED) {
            releaseReservations(order);
        }

        SalesOrderStatus before = order.getStatus();
        order.setStatus(SalesOrderStatus.CANCELLED);
        SalesOrder saved = salesOrderRepository.save(order);

        auditLogService.logChange(ErpModule.SALES, saved.getId(), saved.getReference(),
                AuditAction.UPDATE, "status", before, saved.getStatus(), currentUsername());
        return toResponse(saved);
    }

    @Transactional
    public void deleteOrder(Long id) {
        SalesOrder order = findWithDetails(id);
        ensureDraft(order);
        order.setActive(false);
        salesOrderRepository.save(order);

        auditLogService.logChange(ErpModule.SALES, order.getId(), order.getReference(),
                AuditAction.DELETE, "active", true, false, currentUsername());
    }

    private List<SalesOrderLine> buildLines(SalesOrder order, List<SalesOrderLineRequest> lineRequests) {
        return lineRequests.stream()
                .map(req -> {
                    Product product = productRepository.findById(req.getProductId())
                            .filter(Product::isActive)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST, "Product not found: " + req.getProductId()));

                    if (product.getProductType() != ProductType.FINISHED_GOOD) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Sales orders can only include finished goods. "
                                        + product.getReference() + " is not a finished good.");
                    }

                    BigDecimal unitPrice = req.getUnitPrice() != null
                            ? req.getUnitPrice()
                            : product.getSalesPrice();

                    return SalesOrderLine.builder()
                            .salesOrder(order)
                            .product(product)
                            .orderedQty(req.getOrderedQty())
                            .deliveredQty(BigDecimal.ZERO)
                            .unitPrice(unitPrice)
                            .units(req.getUnits())
                            .availabilityShortage(false)
                            .build();
                })
                .toList();
    }

    private boolean belongsToSalesPerson(SalesOrder order, Long userId) {
    return order.getSalesPerson() != null && order.getSalesPerson().getId().equals(userId);
}

    private Long currentUserId() {
        return currentUser().getId();
    }

    private AppUser currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    private void releaseReservations(SalesOrder order) {
        for (SalesOrderLine line : order.getLines()) {
            BigDecimal pending = line.getPendingQty();
            if (pending.compareTo(BigDecimal.ZERO) > 0) {
                Product product = line.getProduct();
                product.setReservedQty(product.getReservedQty().subtract(pending));
                productRepository.save(product);
            }
        }
    }

    private SalesOrderStatus resolveDeliveryStatus(SalesOrder order) {
        boolean allDelivered = order.getLines().stream()
                .allMatch(line -> line.getDeliveredQty().compareTo(line.getOrderedQty()) == 0);
        if (allDelivered) {
            return SalesOrderStatus.FULLY_DELIVERED;
        }
        boolean anyDelivered = order.getLines().stream()
                .anyMatch(line -> line.getDeliveredQty().compareTo(BigDecimal.ZERO) > 0);
        return anyDelivered ? SalesOrderStatus.PARTIALLY_DELIVERED : order.getStatus();
    }

    private boolean isLate(SalesOrder order) {
        return order.getStatus() == SalesOrderStatus.CONFIRMED
                && order.getStartDate().isBefore(LocalDate.now());
    }

    private boolean matchesSearch(SalesOrder order, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String q = search.trim().toLowerCase();
        return order.getReference().toLowerCase().contains(q)
                || order.getCustomer().getName().toLowerCase().contains(q);
    }

    private SalesOrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return SalesOrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

    private String resolveAddress(String requested, Customer customer) {
        if (requested != null && !requested.isBlank()) {
            return requested.trim();
        }
        return customer.getAddress() != null ? customer.getAddress() : "";
    }

    private AppUser resolveSalesPerson(Long salesPersonId) {
        if (salesPersonId != null) {
            return userRepository.findById(salesPersonId)
                    .filter(AppUser::isActive)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sales person not found"));
        }
        return currentUser();
    }

    private SalesOrder findWithDetails(Long id) {
        return salesOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sales order not found"));
    }

    private void ensureDraft(SalesOrder order) {
        if (order.getStatus() != SalesOrderStatus.DRAFT) {
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

    private BigDecimal calculateOrderTotal(SalesOrder order) {
        return order.getLines().stream()
                .map(SalesOrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private SalesOrderListResponse toListResponse(SalesOrder order) {
        return SalesOrderListResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .startDate(order.getStartDate())
                .customerName(order.getCustomer().getName())
                .salesPersonName(order.getSalesPerson() != null ? order.getSalesPerson().getFullName() : null)
                .status(order.getStatus())
                .orderTotal(calculateOrderTotal(order))
                .build();
    }

    private SalesOrderResponse toResponse(SalesOrder order) {
        return SalesOrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .customerAddress(order.getCustomerAddress())
                .creationDate(order.getCreationDate())
                .startDate(order.getStartDate())
                .salesPersonId(order.getSalesPerson() != null ? order.getSalesPerson().getId() : null)
                .salesPersonName(order.getSalesPerson() != null ? order.getSalesPerson().getFullName() : null)
                .orderTotal(calculateOrderTotal(order))
                .lines(order.getLines().stream().map(this::toLineResponse).toList())
                .build();
    }

    private SalesOrderLineResponse toLineResponse(SalesOrderLine line) {
        return SalesOrderLineResponse.builder()
                .id(line.getId())
                .productId(line.getProduct().getId())
                .productReference(line.getProduct().getReference())
                .productName(line.getProduct().getName())
                .orderedQty(line.getOrderedQty())
                .deliveredQty(line.getDeliveredQty())
                .unitPrice(line.getUnitPrice())
                .units(line.getUnits())
                .availabilityShortage(line.isAvailabilityShortage())
                .lineTotal(line.getLineTotal())
                .freeToUseQty(line.getProduct().getFreeToUseQty())
                .build();
    }
}
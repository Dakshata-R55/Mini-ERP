package com.Mini_ERP.service;

import com.Mini_ERP.dto.*;
import com.Mini_ERP.model.*;
import com.Mini_ERP.repository.ManufacturingOrderRepository;
import com.Mini_ERP.repository.PurchaseOrderRepository;
import com.Mini_ERP.repository.SalesOrderRepository;
import com.Mini_ERP.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final PermissionService permissionService;

    public DashboardSummaryResponse getSummary() {
        AppUser currentUser = currentUser();

        return DashboardSummaryResponse.builder()
                .salesOrders(canRead(currentUser, ErpModule.SALES)
                        ? buildSalesStats(currentUser) : null)
                .purchaseOrders(canRead(currentUser, ErpModule.PURCHASE)
                        ? buildPurchaseStats(currentUser) : null)
                .manufacturingOrders(canRead(currentUser, ErpModule.MANUFACTURING)
                        ? buildManufacturingStats(currentUser) : null)
                .build();
    }

    private OrderModuleDashboardResponse buildSalesStats(AppUser currentUser) {
        List<SalesOrder> orders = salesOrderRepository.findAllActiveWithCustomer();
        return OrderModuleDashboardResponse.builder()
                .all(countSales(orders, null))
                .mine(countSales(orders, currentUser.getId()))
                .build();
    }

    private OrderModuleDashboardResponse buildPurchaseStats(AppUser currentUser) {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAllActiveWithVendor();
        return OrderModuleDashboardResponse.builder()
                .all(countPurchase(orders, null))
                .mine(countPurchase(orders, currentUser.getId()))
                .build();
    }

    private OrderModuleDashboardResponse buildManufacturingStats(AppUser currentUser) {
        List<ManufacturingOrder> orders = manufacturingOrderRepository.findAllActiveWithAssignee();
        return OrderModuleDashboardResponse.builder()
                .all(countManufacturing(orders, null))
                .mine(countManufacturing(orders, currentUser.getId()))
                .build();
    }

    private OrderStatusCountsResponse countSales(List<SalesOrder> orders, Long userId) {
        OrderStatusCountsResponse counts = emptyCounts();
        for (SalesOrder order : orders) {
            if (userId != null && !belongsToUser(order.getSalesPerson(), userId)) {
                continue;
            }
            switch (order.getStatus()) {
                case DRAFT -> counts.setDraft(counts.getDraft() + 1);
                case CONFIRMED -> counts.setConfirmed(counts.getConfirmed() + 1);
                case PARTIALLY_DELIVERED -> counts.setPartial(counts.getPartial() + 1);
                case FULLY_DELIVERED -> counts.setCompleted(counts.getCompleted() + 1);
                default -> { /* CANCELLED ignored on dashboard */ }
            }
            if (isSalesLate(order)) {
                counts.setLate(counts.getLate() + 1);
            }
        }
        return counts;
    }

    private OrderStatusCountsResponse countPurchase(List<PurchaseOrder> orders, Long userId) {
        OrderStatusCountsResponse counts = emptyCounts();
        for (PurchaseOrder order : orders) {
            if (userId != null && !belongsToUser(order.getResponsiblePerson(), userId)) {
                continue;
            }
            switch (order.getStatus()) {
                case DRAFT -> counts.setDraft(counts.getDraft() + 1);
                case CONFIRMED -> counts.setConfirmed(counts.getConfirmed() + 1);
                case PARTIALLY_RECEIVED -> counts.setPartial(counts.getPartial() + 1);
                case FULLY_RECEIVED -> counts.setCompleted(counts.getCompleted() + 1);
                default -> { }
            }
            if (isPurchaseLate(order)) {
                counts.setLate(counts.getLate() + 1);
            }
        }
        return counts;
    }

    private OrderStatusCountsResponse countManufacturing(List<ManufacturingOrder> orders, Long userId) {
        OrderStatusCountsResponse counts = emptyCounts();
        for (ManufacturingOrder order : orders) {
            if (userId != null && !belongsToUser(order.getAssignee(), userId)) {
                continue;
            }
            switch (order.getStatus()) {
                case DRAFT -> counts.setDraft(counts.getDraft() + 1);
                case CONFIRMED -> counts.setConfirmed(counts.getConfirmed() + 1);
                case IN_PROGRESS -> counts.setInProgress(counts.getInProgress() + 1);
                case TO_CLOSE -> counts.setToClose(counts.getToClose() + 1);
                case DONE -> counts.setCompleted(counts.getCompleted() + 1);
                default -> { }
            }
        }
        return counts;
    }

    private boolean isSalesLate(SalesOrder order) {
        return order.getStatus() == SalesOrderStatus.CONFIRMED
                && order.getStartDate().isBefore(LocalDate.now());
    }

    private boolean isPurchaseLate(PurchaseOrder order) {
        return (order.getStatus() == PurchaseOrderStatus.CONFIRMED
                || order.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED)
                && order.getStartDate().isBefore(LocalDate.now());
    }

    private boolean belongsToUser(AppUser person, Long userId) {
        return person != null && person.getId().equals(userId);
    }

    private boolean canRead(AppUser user, ErpModule module) {
        return permissionService.canRead(user, module);
    }

    private OrderStatusCountsResponse emptyCounts() {
        return OrderStatusCountsResponse.builder().build();
    }

    private AppUser currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
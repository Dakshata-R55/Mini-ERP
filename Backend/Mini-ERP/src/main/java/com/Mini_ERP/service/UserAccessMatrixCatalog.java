package com.Mini_ERP.service;

import com.Mini_ERP.dto.AccessMatrixResponse;
import com.Mini_ERP.dto.AccessMatrixResponse.AccessMatrixRow;
import com.Mini_ERP.dto.AccessMatrixResponse.AccessMatrixTab;
import com.Mini_ERP.model.UserType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAccessMatrixCatalog {

    public AccessMatrixResponse matrixFor(UserType userType) {
        return AccessMatrixResponse.builder()
                .tabs(List.of(
                        salesTab(userType),
                        purchaseTab(userType),
                        manufacturingTab(userType),
                        productTab(userType)
                ))
                .build();
    }

    private AccessMatrixTab salesTab(UserType userType) {
        return tab("SALES", "Sales", switch (userType) {
            case SALES_USER, ADMIN -> salesUserRows();
            case NONE -> noAccessRows();
            default -> readOnlyModuleRows(userType);
        });
    }

    private AccessMatrixTab purchaseTab(UserType userType) {
        return tab("PURCHASE", "Purchase", switch (userType) {
            case PURCHASE_USER, ADMIN -> purchaseUserRows();
            case NONE -> noAccessRows();
            default -> readOnlyModuleRows(userType);
        });
    }

    private AccessMatrixTab manufacturingTab(UserType userType) {
        return tab("MANUFACTURING", "Manufacturing", switch (userType) {
            case MANUFACTURING_USER, ADMIN -> manufacturingUserRows();
            case PROJECT_MANAGER -> projectManagerManufacturingRows();
            case NONE -> noAccessRows();
            default -> readOnlyModuleRows(userType);
        });
    }

    private AccessMatrixTab productTab(UserType userType) {
        return tab("PRODUCT", "Product", switch (userType) {
            case PROJECT_MANAGER, ADMIN -> productManagerRows();
            case NONE -> noAccessRows();
            default -> readOnlyModuleRows(userType);
        });
    }

    private AccessMatrixTab tab(String key, String label, List<AccessMatrixRow> rows) {
        return AccessMatrixTab.builder().key(key).label(label).rows(rows).build();
    }

    private List<AccessMatrixRow> noAccessRows() {
        return List.of(row("Module access", no(), no(), no(), no()));
    }

    private List<AccessMatrixRow> readOnlyModuleRows(UserType userType) {
        return List.of(row("Module access", no(), yes(), no(), no(),
                "Read-only visibility for " + userType.name().replace('_', ' ')));
    }

    private List<AccessMatrixRow> salesUserRows() {
        List<AccessMatrixRow> rows = new ArrayList<>();
        rows.add(row("Customer", yes(), yes(), yes(), yes()));
        rows.add(row("Customer Address", yes(), yes(), yes(), yes()));
        rows.add(row("Sales Person", yes(), yes(), yes(), yes()));
        rows.add(row("Product", yes(), yes(), yes(), yes()));
        rows.add(row("Ordered Quantity", yes(), yes(), yes(), yes()));
        rows.add(row("Delivered Quantity", yes(), yes(), yes(), yes()));
        rows.add(row("Sales Price", yes(), yes(), yes(), yes()));
        rows.add(row("Status", yes(), yes(), yes(), no()));
        rows.add(row("Total", yes(), yes(), recomputed(), no()));
        rows.add(row("Creation Date", auto(), yes(), no(), no()));
        return rows;
    }

    private List<AccessMatrixRow> purchaseUserRows() {
        List<AccessMatrixRow> rows = new ArrayList<>();
        rows.add(row("Vendor", yes(), yes(), yes(), yes()));
        rows.add(row("Vendor Address", yes(), yes(), yes(), yes()));
        rows.add(row("Responsible Person", yes(), yes(), yes(), yes()));
        rows.add(row("Product", yes(), yes(), yes(), yes()));
        rows.add(row("Ordered Quantity", yes(), yes(), yes(), yes()));
        rows.add(row("Received Quantity", yes(), yes(), yes(), yes()));
        rows.add(row("Cost Price", yes(), yes(), yes(), yes()));
        rows.add(row("Total", yes(), yes(), recomputed(), no()));
        rows.add(row("Creation Date", auto(), yes(), no(), no()));
        return rows;
    }

    private List<AccessMatrixRow> manufacturingUserRows() {
        List<AccessMatrixRow> rows = new ArrayList<>();
        rows.add(row("Product to Manufacture", yes(), yes(), yes(), yes()));
        rows.add(row("Product Quantity", yes(), yes(), yes(), yes()));
        rows.add(row("BOM", yes(), yes(), yes(), yes()));
        rows.add(row("Responsible Person", yes(), yes(), yes(), yes()));
        rows.add(row("Finished Quantity", yes(), yes(), yes(), yes()));
        rows.add(row("Creation Date", auto(), yes(), no(), no()));
        return rows;
    }

    private List<AccessMatrixRow> projectManagerManufacturingRows() {
        List<AccessMatrixRow> rows = new ArrayList<>();
        rows.add(row("Manufacturing Orders", yes(), yes(), yes(), yes()));
        rows.add(row("BOM", yes(), yes(), yes(), yes()));
        rows.add(row("Work Centers", yes(), yes(), yes(), yes()));
        rows.add(row("Creation Date", auto(), yes(), no(), no()));
        return rows;
    }

    private List<AccessMatrixRow> productManagerRows() {
        List<AccessMatrixRow> rows = new ArrayList<>();
        rows.add(row("Product", yes(), yes(), yes(), yes()));
        rows.add(row("Sales Price", yes(), yes(), yes(), yes()));
        rows.add(row("Cost Price", yes(), yes(), yes(), yes()));
        rows.add(row("On Hand Qty", yes(), yes(), yes(), no()));
        rows.add(row("Free To Use Qty", yes(), yes(), systemComputed(), no()));
        rows.add(row("Procure On Demand", notPossible(), yes(), yes(), yes()));
        rows.add(row("Procurement Method", notPossible(), yes(), yes(), yes()));
        rows.add(row("Vendor", yes(), yes(), yes(), yes()));
        rows.add(row("Bill of Materials (BoM)", yes(), yes(), yes(), yes()));
        return rows;
    }

    private AccessMatrixRow row(String field, String create, String view, String edit, String delete) {
        return AccessMatrixRow.builder()
                .field(field)
                .create(create)
                .view(view)
                .edit(edit)
                .delete(delete)
                .build();
    }

    private AccessMatrixRow row(String field, String create, String view, String edit, String delete, String note) {
        return AccessMatrixRow.builder()
                .field(field + " (" + note + ")")
                .create(create)
                .view(view)
                .edit(edit)
                .delete(delete)
                .build();
    }

    private String yes() {
        return "YES";
    }

    private String no() {
        return "NO";
    }

    private String auto() {
        return "AUTO";
    }

    private String recomputed() {
        return "RECOMPUTED";
    }

    private String notPossible() {
        return "NOT_POSSIBLE";
    }

    private String systemComputed() {
        return "SYSTEM_COMPUTED";
    }
}

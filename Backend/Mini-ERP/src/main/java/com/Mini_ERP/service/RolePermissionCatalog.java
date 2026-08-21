package com.Mini_ERP.service;

import com.Mini_ERP.model.*;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class RolePermissionCatalog {

    public void apply(AppUser user, UserType type) {
        user.getModulePermissions().clear();
        user.setUserType(type);

        // System Admin: user-role management only — no Sales/Purchase/Products/etc.
        if (type == UserType.SYSTEM_ADMIN) {
            for (ErpModule module : ErpModule.values()) {
                if (module == ErpModule.USER_MANAGEMENT) {
                    addPermission(user, module, AccessLevel.ADMIN, true);
                } else {
                    addPermission(user, module, AccessLevel.NONE, false);
                }
            }
            return;
        }

        // Business ADMIN: full access to all modules
        if (type == UserType.ADMIN) {
            for (ErpModule module : ErpModule.values()) {
                addPermission(user, module, AccessLevel.ADMIN, true);
            }
            return;
        }

        Map<ErpModule, AccessLevel> defaults = defaultsFor(type);
        for (ErpModule module : ErpModule.values()) {
            AccessLevel level = defaults.getOrDefault(module, AccessLevel.NONE);
            boolean canRead = shouldAllowRead(type, module, level);
            addPermission(user, module, level, canRead);
        }
    }

    private Map<ErpModule, AccessLevel> defaultsFor(UserType type) {
        Map<ErpModule, AccessLevel> map = new EnumMap<>(ErpModule.class);

        switch (type) {
            case SALES_USER -> map.put(ErpModule.SALES, AccessLevel.USER);
            case PURCHASE_USER -> map.put(ErpModule.PURCHASE, AccessLevel.USER);
            case MANUFACTURING_USER -> {
                map.put(ErpModule.MANUFACTURING, AccessLevel.USER);
                map.put(ErpModule.BOM, AccessLevel.USER);
            }
            case PROJECT_MANAGER -> {
                map.put(ErpModule.PRODUCTS, AccessLevel.ADMIN);
                map.put(ErpModule.DASHBOARD, AccessLevel.USER);
            }
            default -> { }
        }

        return map;
    }

    private boolean shouldAllowRead(UserType type, ErpModule module, AccessLevel level) {
        if (level != AccessLevel.NONE) {
            return true;
        }
        if (type == UserType.NONE || type == UserType.SYSTEM_ADMIN) {
            return false;
        }
        return module == ErpModule.PRODUCTS || module == ErpModule.DASHBOARD;
    }

    private void addPermission(AppUser user, ErpModule module, AccessLevel level, boolean canRead) {
        ModulePermission permission = ModulePermission.builder()
                .user(user)
                .module(module)
                .accessLevel(level)
                .canRead(canRead)
                .build();
        user.getModulePermissions().add(permission);
    }
}
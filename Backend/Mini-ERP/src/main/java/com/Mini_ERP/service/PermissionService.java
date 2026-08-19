package com.Mini_ERP.service;

import com.Mini_ERP.model.AccessLevel;
import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.model.ModulePermission;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {

    public boolean canRead(AppUser user, ErpModule module) {
        if (user.isSystemAdmin()) {
            return true;
        }
        return getPermission(user, module)
                .map(p -> p.getAccessLevel() != AccessLevel.NONE || p.isCanRead())
                .orElse(false);
    }

    public boolean canWrite(AppUser user, ErpModule module) {
        if (user.isSystemAdmin()) {
            return true;
        }
        return getPermission(user, module)
                .map(p -> p.getAccessLevel() == AccessLevel.ADMIN
                        || p.getAccessLevel() == AccessLevel.USER)
                .orElse(false);
    }

    public boolean canAdmin(AppUser user, ErpModule module) {
        if (user.isSystemAdmin()) {
            return true;
        }
        return getPermission(user, module)
                .map(p -> p.getAccessLevel() == AccessLevel.ADMIN)
                .orElse(false);
    }

    private Optional<ModulePermission> getPermission(AppUser user, ErpModule module) {
        return user.getModulePermissions().stream()
                .filter(p -> p.getModule() == module)
                .findFirst();
    }
}
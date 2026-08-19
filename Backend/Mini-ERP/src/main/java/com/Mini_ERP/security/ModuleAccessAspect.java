package com.Mini_ERP.security;

import com.Mini_ERP.model.AppUser;
import com.Mini_ERP.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ModuleAccessAspect {

    private final PermissionService permissionService;

    @Before("@annotation(requires)")
    public void checkAccess(RequiresModuleAccess requires) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new AccessDeniedException("Unauthorized");
        }

        AppUser user = customUserDetails.getUser();

        boolean allowed = switch (requires.action()) {
            case READ -> permissionService.canRead(user, requires.module());
            case WRITE -> permissionService.canWrite(user, requires.module());
            case ADMIN -> permissionService.canAdmin(user, requires.module());
        };

        if (!allowed) {
            throw new AccessDeniedException(
                    "No " + requires.action() + " access on " + requires.module());
        }
    }
}
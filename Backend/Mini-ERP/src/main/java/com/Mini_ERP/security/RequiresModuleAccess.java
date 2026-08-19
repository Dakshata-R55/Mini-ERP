package com.Mini_ERP.security;

import com.Mini_ERP.model.ErpModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresModuleAccess {

    ErpModule module();

    Action action();

    enum Action {
        READ,
        WRITE,
        ADMIN
    }
}
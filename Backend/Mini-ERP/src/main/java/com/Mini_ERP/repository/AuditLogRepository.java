package com.Mini_ERP.repository;

import com.Mini_ERP.model.AuditLog;
import com.Mini_ERP.model.ErpModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByModuleAndEntityIdOrderByChangedAtDesc(ErpModule module, Long entityId);

    List<AuditLog> findByModuleOrderByChangedAtDesc(ErpModule module);
}
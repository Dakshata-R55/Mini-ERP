package com.Mini_ERP.repository;

import com.Mini_ERP.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByIdAndActiveTrue(Long id);

    Optional<PurchaseOrder> findTopByOrderByIdDesc();

    @Query("""
            SELECT DISTINCT po FROM PurchaseOrder po
            JOIN FETCH po.vendor v
            LEFT JOIN FETCH po.responsiblePerson rp
            LEFT JOIN FETCH po.lines l
            LEFT JOIN FETCH l.product
            WHERE po.id = :id AND po.active = true
            """)
    Optional<PurchaseOrder> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT po FROM PurchaseOrder po
            JOIN FETCH po.vendor v
            LEFT JOIN FETCH po.responsiblePerson rp
            LEFT JOIN FETCH po.lines l
            LEFT JOIN FETCH l.product
            WHERE po.active = true
            ORDER BY po.creationDate DESC
            """)
    List<PurchaseOrder> findAllActiveWithVendor();
}
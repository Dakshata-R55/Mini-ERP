package com.Mini_ERP.repository;

import com.Mini_ERP.model.ManufacturingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {

    @Query("""
        SELECT m FROM ManufacturingOrder m
        JOIN FETCH m.finishedProduct
        LEFT JOIN FETCH m.assignee
        WHERE m.active = true
        ORDER BY m.id DESC
        """)
List<ManufacturingOrder> findAllActiveWithAssignee();

    @Query("SELECT m FROM ManufacturingOrder m JOIN FETCH m.finishedProduct WHERE m.active = true ORDER BY m.id DESC")
    List<ManufacturingOrder> findAllActiveWithProduct();

    @Query("SELECT m FROM ManufacturingOrder m JOIN FETCH m.finishedProduct LEFT JOIN FETCH m.billOfMaterial LEFT JOIN FETCH m.assignee LEFT JOIN FETCH m.components c LEFT JOIN FETCH c.product LEFT JOIN FETCH m.workOrders w LEFT JOIN FETCH w.workCenter WHERE m.id = :id AND m.active = true")
    Optional<ManufacturingOrder> findActiveWithDetails(Long id);

    Optional<ManufacturingOrder> findTopByOrderByIdDesc();
}

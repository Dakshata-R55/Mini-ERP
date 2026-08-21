package com.Mini_ERP.repository;

import com.Mini_ERP.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByIdAndActiveTrue(Long id);

    Optional<SalesOrder> findTopByOrderByIdDesc();

    @Query("""
            SELECT DISTINCT so FROM SalesOrder so
            JOIN FETCH so.customer c
            LEFT JOIN FETCH so.salesPerson sp
            LEFT JOIN FETCH so.lines l
            LEFT JOIN FETCH l.product
            WHERE so.id = :id AND so.active = true
            """)
    Optional<SalesOrder> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT so FROM SalesOrder so
            JOIN FETCH so.customer c
            LEFT JOIN FETCH so.salesPerson sp
            LEFT JOIN FETCH so.lines l
            LEFT JOIN FETCH l.product
            WHERE so.active = true
            ORDER BY so.creationDate DESC
            """)
    List<SalesOrder> findAllActiveWithCustomer();
}
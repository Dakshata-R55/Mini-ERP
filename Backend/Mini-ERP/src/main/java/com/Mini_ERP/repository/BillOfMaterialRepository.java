package com.Mini_ERP.repository;

import com.Mini_ERP.model.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {

    @Query("SELECT b FROM BillOfMaterial b JOIN FETCH b.finishedProduct WHERE b.active = true ORDER BY b.reference")
    List<BillOfMaterial> findAllActiveWithProduct();

    @Query("SELECT b FROM BillOfMaterial b JOIN FETCH b.finishedProduct LEFT JOIN FETCH b.components c LEFT JOIN FETCH c.componentProduct LEFT JOIN FETCH b.operations o LEFT JOIN FETCH o.workCenter WHERE b.id = :id AND b.active = true")
    Optional<BillOfMaterial> findActiveWithDetails(Long id);

    List<BillOfMaterial> findByFinishedProductIdAndActiveTrueOrderByReferenceAsc(Long finishedProductId);

    Optional<BillOfMaterial> findTopByFinishedProductIdAndActiveTrueOrderByCreatedAtDesc(Long finishedProductId);

    Optional<BillOfMaterial> findTopByOrderByIdDesc();
}

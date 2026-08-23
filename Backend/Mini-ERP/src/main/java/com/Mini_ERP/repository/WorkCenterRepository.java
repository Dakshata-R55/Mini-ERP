package com.Mini_ERP.repository;

import com.Mini_ERP.model.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkCenterRepository extends JpaRepository<WorkCenter, Long> {

    List<WorkCenter> findByActiveTrueOrderByNameAsc();

    Optional<WorkCenter> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCase(String name);
}

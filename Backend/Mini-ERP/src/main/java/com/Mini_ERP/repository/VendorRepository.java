package com.Mini_ERP.repository;

import com.Mini_ERP.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    List<Vendor> findByActiveTrueOrderByNameAsc();

    Optional<Vendor> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCase(String name);

    Optional<Vendor> findFirstByActiveTrueOrderByNameAsc();
}

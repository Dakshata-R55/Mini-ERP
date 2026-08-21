package com.Mini_ERP.repository;

import com.Mini_ERP.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByActiveTrueOrderByNameAsc();

    Optional<Customer> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCase(String name);
}
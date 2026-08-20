package com.Mini_ERP.repository;

import com.Mini_ERP.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByReference(String reference);

    boolean existsByReference(String reference);

    Optional<Product> findTopByOrderByIdDesc();
}
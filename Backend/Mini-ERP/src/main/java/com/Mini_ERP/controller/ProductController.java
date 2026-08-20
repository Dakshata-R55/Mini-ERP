package com.Mini_ERP.controller;

import com.Mini_ERP.dto.ProductListResponse;
import com.Mini_ERP.dto.ProductRequest;
import com.Mini_ERP.dto.ProductResponse;
import com.Mini_ERP.model.ErpModule;
import com.Mini_ERP.security.RequiresModuleAccess;
import com.Mini_ERP.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @RequiresModuleAccess(module = ErpModule.PRODUCTS, action = RequiresModuleAccess.Action.READ)
    public List<ProductListResponse> listProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PRODUCTS, action = RequiresModuleAccess.Action.READ)
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @RequiresModuleAccess(module = ErpModule.PRODUCTS, action = RequiresModuleAccess.Action.WRITE)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PRODUCTS, action = RequiresModuleAccess.Action.WRITE)
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @RequiresModuleAccess(module = ErpModule.PRODUCTS, action = RequiresModuleAccess.Action.ADMIN)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
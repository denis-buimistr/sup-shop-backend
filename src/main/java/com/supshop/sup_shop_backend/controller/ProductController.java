package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.ProductFilter;
import com.supshop.sup_shop_backend.dto.ProductRequest;
import com.supshop.sup_shop_backend.dto.ProductResponse;
import com.supshop.sup_shop_backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductResponse> getAll(ProductFilter filter, Pageable pageable) {
        return productService.findAll(filter, pageable);
    }

    // для публичной страницы товара; по id — для админки
    @GetMapping("/slug/{slug}")
    public ProductResponse getBySlug(@PathVariable String slug) {
        return productService.findBySlug(slug);
    }

    @GetMapping("/{id}")
    public ProductResponse getByID(@PathVariable Long id) {
        return productService.findByID(id);
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}

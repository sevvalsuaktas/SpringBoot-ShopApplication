package com.example.shop.shop.controller;

import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Tüm ürünler
     * Örneğin: GET /api/v1/products?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    /**
     * İsim araması
     * Örneğin: GET /api/v1/products/search?name=phone&page=0&size=5
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByName(name, pageable));
    }

    /**
     * Kategoriye göre filtre
     * Örneğin: GET /api/v1/products/filter?categoryId=3&page=0&size=5
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto>> filterByCategory(
            @RequestParam Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.filterByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Validated @RequestBody ProductDto dto) {
        ProductDto created = productService.create(dto);
        return ResponseEntity
                .status(201)
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable Long id,
            @Validated @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


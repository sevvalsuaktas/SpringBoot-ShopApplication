package com.example.shop.shop.controller;

import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @Loggable
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll() {
        List<ProductDto> products = productService.getAll();
        return ResponseEntity.ok(products);
    }

    @Loggable
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@Valid @PathVariable Long id) {
        log.info("GET /api/v1/products/{} çağrıldı", id);
        ProductDto dto = productService.getById(id);
        log.debug("ÜRÜN DÖNÜYOR: {}", dto);
        return ResponseEntity.ok(dto);
    }

    @Loggable
    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        ProductDto created = productService.create(dto);
        return ResponseEntity
                .status(201)
                .body(created);
    }

    @Loggable
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @Loggable
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Valid @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
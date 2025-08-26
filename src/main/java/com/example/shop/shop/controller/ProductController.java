package com.example.shop.shop.controller;

import com.example.shop.shop.dto.ProductDto;
import com.example.shop.shop.logging.Loggable;
import com.example.shop.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
    @GetMapping("/{id}") // id si girilen product ı getiriyor
    public ResponseEntity<ProductDto> getById(@PathVariable Long id) {
        log.info("GET /api/v1/products/{} çağrıldı", id);
        ProductDto dto = productService.getById(id);
        log.debug("ÜRÜN DÖNÜYOR: {}", dto);
        return ResponseEntity.ok(dto);
    }

    @Loggable
    @PostMapping // yeni bir ürün ekleme endpointi
    public ResponseEntity<ProductDto> create(@Validated @RequestBody ProductDto dto) {
        ProductDto created = productService.create(dto);
        return ResponseEntity
                .status(201)
                .body(created); // response olarak 201 created dönüyor
    }

    @Loggable
    @PutMapping("/{id}") // id si girilen ürünü güncellemeye yarayan endpoint
    public ResponseEntity<ProductDto> update(
            @PathVariable Long id,
            @Validated @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @Loggable
    @DeleteMapping("/{id}") // id si girilen ürünü silen endpoint
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /*
    @Loggable
    @GetMapping("/products/search")
    public ResponseEntity<List<ProductDto>> search(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(productService.searchByName(name));
    }

    @Loggable
    @GetMapping("/products/filter")
    public ResponseEntity<List<ProductDto>> filterByCategory(
            @RequestParam Long categoryId
    ) {
        return ResponseEntity.ok(productService.filterByCategory(categoryId));
    }*/
}


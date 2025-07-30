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

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Loggable
    @GetMapping // tüm ürünleri pageable listeleyen liste döner
    public ResponseEntity<Page<ProductDto>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @Loggable
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchByName( // isme göre arama yapıyor
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.searchByName(name, pageable));
    }

    @Loggable
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto>> filterByCategory( // kategorilere göre arama yapıyor
            @RequestParam Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.filterByCategory(categoryId, pageable));
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
}


package com.example.shop.shop.controller;

import com.example.shop.shop.dto.CategoryDto;
import com.example.shop.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll() { // veri tabanından var olan kategorileri çeker
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/{id}") // istenen id ye göre kategoriyi çeker
    public ResponseEntity<CategoryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping // yeni kategori oluşturmak için endpoint
    public ResponseEntity<CategoryDto> create(@Validated @RequestBody CategoryDto dto) {
        CategoryDto created = categoryService.create(dto);
        return ResponseEntity
                .status(201)
                .body(created); // response olarak 201 created döner
    }

    @PutMapping("/{id}") // girilen id yi güncellemek için endpoint
    public ResponseEntity<CategoryDto> update(
            @PathVariable Long id,
            @Validated @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}") // girilen idye göre silme işlemi yapan endpoint
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

